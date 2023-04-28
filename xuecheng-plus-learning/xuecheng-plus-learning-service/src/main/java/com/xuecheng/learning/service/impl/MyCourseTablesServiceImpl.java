package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    ContentServiceClient contentServiceClient;


    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {

        //选课调用内容管理查询课程的收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            XueChengPlusException.cast("课程不存在");
        }
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse = null;
        if("201000".equals(charge)){
            //如果课程免费，会向选课记录表、我的课程表写数据
            xcChooseCourse = addFreeCoruse(userId, coursepublish);
            //向我的课程表写
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);
        }else {
            //如果课程收费，会向选课记录表写数据
            xcChooseCourse = addChargeCoruse(userId, coursepublish);
        }

        //判断学生的学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        //构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        //设置学习资格状态
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        //查询我的课程表，如果查不到说明没有选课
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables == null){
            xcCourseTablesDto.setLearnStatus("702002");//没有选课或选课后没有支付
            return xcCourseTablesDto;
        }
        //查到了，判断是否过期
        if(xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now())){
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702003");//已过期需要申请续期或者重新支付
            return xcCourseTablesDto;
        }else {
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702001");//正常学习
            return xcCourseTablesDto;
        }
    }

    @Transactional
    @Override
    public boolean saveChooseCourseSuccess(String chooseCourseId) {

        //根据选课id查询选课表
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if(xcChooseCourse == null){
            log.debug("接收到购买课程的消息，根据选课id从数据库找不到选课记录：{}", chooseCourseId);
            return false;
        }
        String status = xcChooseCourse.getStatus();
        if("701001".equals(status)){
            return true;
        }
        //更新选课记录的状态为支付成功
        xcChooseCourse.setStatus("701001");
        int i = xcChooseCourseMapper.updateById(xcChooseCourse);
        if(i <= 0){
            log.debug("添加选课记录失败:{}", xcChooseCourse);
            XueChengPlusException.cast("添加选课记录失败");
        }
        //向我的课程表插入记录
        addCourseTabls(xcChooseCourse);
        return true;
    }

    @Override
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params) {

        String userId = params.getUserId();
        //页码
        int pageNo = params.getPage();
        //每页记录数
        int size = params.getSize();
        Page<XcCourseTables> xcCourseTablesPage = new Page<>(pageNo, size);

        LambdaQueryWrapper<XcCourseTables> eq = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId);

        Page<XcCourseTables> result = xcCourseTablesMapper.selectPage(xcCourseTablesPage, eq);

        List<XcCourseTables> records = result.getRecords();

        return new PageResult<>(records, (int)result.getTotal(), pageNo, size);
    }

    //添加免费课程,免费课程加入选课记录表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {

        Long courseId = coursepublish.getId();

        //如果存在免费的选课记录且选课状态为成功，直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001");//选课成功

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses != null && xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }

        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(courseId);
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("700001");
        chooseCourse.setCoursePrice(coursepublish.getPrice());
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001");
        chooseCourse.setValidtimeStart(LocalDateTime.now());//有效期开始时间
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(chooseCourse);
        if(insert <= 0){
            XueChengPlusException.cast("添加选课记录失败");
        }

        return chooseCourse;
    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        Long courseId = coursepublish.getId();

        //如果存在收费的选课记录且选课状态为待支付，直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700002")//收费课程
                .eq(XcChooseCourse::getStatus, "701002");//待支付

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size() > 0){
            return xcChooseCourses.get(0);
        }

        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(courseId);
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("700002");//收费课程
        chooseCourse.setCoursePrice(coursepublish.getPrice());
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701002");//待支付
        chooseCourse.setValidtimeStart(LocalDateTime.now());//有效期开始时间
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(chooseCourse);
        if(insert <= 0){
            XueChengPlusException.cast("添加选课记录失败");
        }

        return chooseCourse;
    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){

        //选课成功了才可以向我的课程表添加
        String status = xcChooseCourse.getStatus();

        if(!"701001".equals(status)){
            XueChengPlusException.cast("选课没有成功，无法添加到课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables!=null){
            return xcCourseTables;
        }

        xcCourseTables = new XcCourseTables();

        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());//记录选课表的课程
        xcCourseTables.setCourseType(xcCourseTables.getCourseType());
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());

        int insert = xcCourseTablesMapper.insert(xcCourseTables);

        if(insert <= 0){
            XueChengPlusException.cast("添加课程到我的课程失败");
        }
        return xcCourseTables;
    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @author Mr.M
     * @date 2022/10/2 17:07
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        return xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
    }
}
