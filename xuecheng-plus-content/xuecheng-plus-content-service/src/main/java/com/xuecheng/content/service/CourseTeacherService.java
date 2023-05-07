package com.xuecheng.content.service;

import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.dto.CompareWithLastYear;
import com.xuecheng.content.model.dto.CourseTeacherDto;

import java.util.List;

public interface CourseTeacherService {

    List<CourseTeacherDto> getCourseTeachers(Long companyId);

    CourseTeacherDto addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto);

    CourseTeacherDto editCourseTeacher(CourseTeacherDto courseTeacherDto);

    void deleteCourseTeacher(Long courseId, Long id);
    void deleteCourseTeachers(Long courseId);

    ResultResponse<CompareWithLastYear> compareWithLastYear();
}
