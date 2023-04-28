package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.CourseNotifyConfig;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class ReceiveCoursePublicNotifyService {

    @Autowired
    MqMessageService messageService;

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    SearchServiceClient searchServiceClient;

    @RabbitListener(queues = CourseNotifyConfig.MINIO_NOTIFY_QUEUE)
    public void process_minio(Message message){

        //转成对象
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);

        Long taskId = mqMessage.getId();
        mqMessage = messageService.getMqMessageById(taskId);
        if(Integer.parseInt(mqMessage.getStageState1()) > 0){
            log.debug("课程静态化任务完成，无需处理.....");
            return;
        }

        // businessKey1存放的是课程id，由消息发送方确定的
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //开始进行课程静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("生成的静态页面为空");
        }
        //上传到minio
        coursePublishService.uploadCourseHtml(courseId, file);

        //任务处理完成，写任务状态为完成
        messageService.completedStageOne(taskId);
    }

    @RabbitListener(queues = CourseNotifyConfig.ES_NOTIFY_QUEUE)
    public void process_es(Message message){

        //转成对象
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);

        Long taskId = mqMessage.getId();
        mqMessage = messageService.getMqMessageById(taskId);
        if(Integer.parseInt(mqMessage.getStageState2()) > 0){
            log.debug("课程索引信息写入已完成，无需处理.....");
            return;
        }

        // businessKey1存放的是课程id，由消息发送方确定的
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //查询课程信息，调用课程搜索服务添加索引接口
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
        }
        messageService.completedStageTwo(taskId);
    }

    @RabbitListener(queues = CourseNotifyConfig.REDIS_NOTIFY_QUEUE)
    public void process_redis(Message message){

        //转成对象
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);

        Long taskId = mqMessage.getId();
        mqMessage = messageService.getMqMessageById(taskId);
        if(Integer.parseInt(mqMessage.getStageState3()) > 0){
            log.debug("已存缓存，无需处理.....");
            return;
        }

        messageService.completedStageThree(taskId);
    }
}
