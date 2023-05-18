package com.xuecheng.content.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.model.dto.CompareWithLastYear;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId){

        return coursePublishService.getCoursePreviewInfo(courseId);
    }

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("用户为登录或者登录过期");
        }
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("用户为登录或者登录过期");
        }
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
//        return coursePublishService.getCoursePublish(courseId);
        return coursePublishService.getCoursePublishCache(courseId);
    }

    @GetMapping("/publish/compareWithLastYear")
    @ResponseBody
    public ResultResponse<CompareWithLastYear> compare(){
        return coursePublishService.compareWithLastYear();
    }

}
