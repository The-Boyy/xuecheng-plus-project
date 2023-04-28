package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTablesService myCourseTablesService;
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {

        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            return RestResponse.validfail("课程不存在");
        }
        //判断是否为试学课程
        //todo：注释处有bug
//        String teachplan = coursepublish.getTeachplan();
//        Map map = JSON.parseObject(teachplan, Map.class);
//        if("1".equals(map.get("isPreview"))){
//            //试学课程
//            //有资格学习，返回视频播放地址
//            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
//        }
        //用户已登录
        if(StringUtils.isNotEmpty(userId)){
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习，因为没有选课或者选课后没有支付");
            }else if("702003".equals(learnStatus)){
                return RestResponse.validfail("无法学习，已过期需要申请续费或重新支付");
            }else {
                //有资格学习，返回视频播放地址
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }
        //用户没有登录
        //取出课程的收费规则
        String charge = coursepublish.getCharge();
        if("201000".equals(charge)){
            //有资格学习，返回视频播放地址
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validfail("该课程没有选课");
    }
}
