package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程信息管理接口
 * @date 2023/2/12 10:14
 */
public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     * @param pageParams 分页查询参数
     * @param courseParamsDto 查询条件
     * @return 查询结果
     */
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    /**
     * 新增课程
     * @param companyId 机构id
     * @param addCourseDto 课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息
     * @param courseId 课程id
     * @return 课程详细信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程
     * @param companyId 机构id
     * @param editCourseDto 修改课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto);


    public void deleteCourseById(Long courseId);

    public void offlineCourseById(Long courseId);

    ResultResponse<List<CategoryWithCountDto>> queryCategoryWithCount();

    ResultResponse<CompareWithLastYear> compareWithLastYear();

    PageResult<AdminCourseInfoDto> queryAdminCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto, Boolean auditFlag);

    ResultResponse<UpdateCourseBaseDto> adminUpdateCourseBase(String username, UpdateCourseBaseDto dto);
}
