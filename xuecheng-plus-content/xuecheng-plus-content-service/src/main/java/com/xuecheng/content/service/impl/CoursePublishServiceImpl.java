package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.config.CourseNotifyConfig;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CompareWithLastYear;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MediaServiceClient mediaServiceClient;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程找不到");
        }

        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();

        //课程审核状态为已提交则不允许提交
        if (auditStatus.equals("202003")) {
            XueChengPlusException.cast("课程已提交请等待审核");
        }

        //课程的图片、计划信息没有填写也不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("请求上传课程图片");
        }
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            XueChengPlusException.cast("请编写课程计划");
        }
        //查询到课程的基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //计划信息
        //转json
        String teachPlanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachPlanTreeJson);
        //状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询预发布表，有记录则更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");

        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {

        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }
        //状态
        String status = coursePublishPre.getStatus();
        //课程如果审核不通过，不允许发布
        if (!status.equals("202004")) {
            XueChengPlusException.cast("课程没有审核通过不允许发布");
        }
        //向课程发布表写数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //先查询课程发布表，有则更新，无则添加
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");//已发布
        int updateCourseBase = courseBaseMapper.updateById(courseBase);
        if(updateCourseBase <= 0){
            XueChengPlusException.cast("修改课程发布信息为已发布失败，因此发布课程失败");
        }
//        //向消息表写入数据
//        saveCoursePublishMessage(courseId);
        //消息通知，通知任务进行minio存储静态页面;es存储课程;redis存储缓存信息;
        notifyCoursePublishMessage(courseId);
        //将预发布表数据删除
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        Configuration configuration = new Configuration(Configuration.getVersion());
        File htmlFile = null;

        try {
            configuration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(),"/templates"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");

            //得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model",coursePreviewInfo);

            //Template template 模板, Object model 数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            //输出文件
            htmlFile = File.createTempFile("coursepublish", ".html");
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream,outputStream);
        }catch (Exception e){
            log.error("页面静态化报错，课程id{}", courseId, e);
            e.printStackTrace();
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {

        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if(upload == null){
                log.debug("远程调用走降级逻辑，得到的上传结果为null，课程id{}",courseId);
                XueChengPlusException.cast("上传静态文件过程中存在异常");
            }
        }catch (Exception e){
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件过程中存在异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {

        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        if(jsonObj != null){
            //返回数据
            String jsonString = jsonObj.toString();
            ///"null"是为了防止缓存穿透
            if("null".equals(jsonString)){
                return null;
            }
            return JSON.parseObject(jsonString, CoursePublish.class);
        }
        //缓存未命中，加锁
        RLock lock = redissonClient.getLock("coursequerylocal:" + courseId);
        lock.lock();
        try {
            //防止其它请求已写缓存
            jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
            if(jsonObj != null){
                //返回数据
                String jsonString = jsonObj.toString();
                if("null".equals(jsonString)){
                    return null;
                }
                return JSON.parseObject(jsonString, CoursePublish.class);
            }
            CoursePublish coursePublish = getCoursePublish(courseId);
            //空对象转JSON后为字符串“null”
            redisTemplate.opsForValue().set("course:"+courseId, JSON.toJSONString(coursePublish), 30, TimeUnit.SECONDS);
            return coursePublish;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteCoursePublishById(Long courseId) {
        int deleteById = coursePublishMapper.deleteById(courseId);
        if(deleteById <= 0){
            XueChengPlusException.cast("删除课程发布信息失败");
        }
    }

    @Override
    public ResultResponse<CompareWithLastYear> compareWithLastYear() {
        LambdaQueryWrapper<CoursePublish> wrapper = new LambdaQueryWrapper<CoursePublish>().orderByDesc(CoursePublish::getCreateDate);

        List<CoursePublish> coursePublishes = coursePublishMapper.selectList(wrapper);

        Map<Integer, Integer> map = new HashMap<>();

        int year = LocalDateTime.now().getYear();
        map.put(year, 0);
        map.put(year - 1, 0);

        for (CoursePublish coursePublish : coursePublishes) {
            int y = coursePublish.getCreateDate().getYear();
            if(map.containsKey(y)){
                map.put(y, map.get(y) + 1);
            }else {
                break;
            }
        }

        CompareWithLastYear compareWithLastYear = new CompareWithLastYear();
        compareWithLastYear.setDate(year + "年");

        Integer curNum = map.get(year);
        Integer lastNum = map.get(year - 1);
        compareWithLastYear.setFlag(curNum >= lastNum);

        compareWithLastYear.setRate((curNum - lastNum) / (lastNum + 0.000001) * 100);

        return ResultResponse.success(200, compareWithLastYear);
    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    public void notifyCoursePublishMessage(Long courseId){
        //生产消息
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
        //消息持久化
        String message = JSON.toJSONString(mqMessage);
        Message messageObj = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        //消息id,唯一
        Long id = mqMessage.getId();
        //correlationData可以指定回调方法
        CorrelationData correlationData = new CorrelationData(id.toString());
        correlationData.getFuture().addCallback(result->{
            boolean ack = result.isAck();
            if(ack){
                //消息发送到了交换机
                log.debug("发送消息成功:{}",message);
            }else {
                //消息发送失败
                log.debug("消息发送失败:{}",message);
                XueChengPlusException.cast("消息发送到交换机发送失败");
            }
        },ex->{
            //发生异常
            log.debug("消息发送发生异常:{}",message);
            XueChengPlusException.cast("消息发送到交换机发生异常");
        });
        rabbitTemplate.convertAndSend(CourseNotifyConfig.COURSE_NOTIFY_EXCHANGE,
                CourseNotifyConfig.COURSE_ROUTING_KEY, messageObj, correlationData);
    }
}
