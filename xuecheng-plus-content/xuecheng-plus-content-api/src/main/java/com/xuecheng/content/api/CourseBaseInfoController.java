package com.xuecheng.content.api;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.model.po.CourseBase;

import java.util.List;

@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController//@Controller和@RespondBody
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        assert user != null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }
        PageResult<CourseBase> result = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParamsDto);

        return result;
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto creatCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        assert user != null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }

        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据课程id查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        assert user != null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }

        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程信息")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourseById(@PathVariable Long courseId){
        courseBaseInfoService.deleteCourseById(courseId);
    }

    @ApiOperation("下架课程")
    @GetMapping("/courseoffline/{courseId}")
    public void offlineCourseById(@PathVariable Long courseId){
        courseBaseInfoService.offlineCourseById(courseId);
    }

    @GetMapping("/hello")
    public String hello(){
        return "hello world";
    }

    @GetMapping("/course/categoryWithCount")
    public ResultResponse<List<CategoryWithCountDto>> queryCategoryWithCount(){
        return courseBaseInfoService.queryCategoryWithCount();
    }

    @GetMapping("course/compareWithLastYear")
    public ResultResponse<CompareWithLastYear> compareWithLastYear(){
        return courseBaseInfoService.compareWithLastYear();
    }
}
