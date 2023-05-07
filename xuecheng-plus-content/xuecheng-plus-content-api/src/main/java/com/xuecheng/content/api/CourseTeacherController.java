package com.xuecheng.content.api;

import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.dto.CompareWithLastYear;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(value = "课程信息管理接口", tags = "课程教师管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程id查询接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacherDto> getCourseTeachers(@PathVariable Long courseId){

        return courseTeacherService.getCourseTeachers(courseId);
    }

    @ApiOperation("添加课程教师")
    @PostMapping("/courseTeacher")
    public CourseTeacherDto addCourseTeacher(@RequestBody AddCourseTeacherDto addCourseTeacherDto){

        return courseTeacherService.addCourseTeacher(addCourseTeacherDto);
    }

    @ApiOperation("编辑课程教师")
    @PutMapping("/courseTeacher")
    public CourseTeacherDto editCourseTeacher(@RequestBody CourseTeacherDto courseTeacherDto){

        return courseTeacherService.editCourseTeacher(courseTeacherDto);
    }

    @ApiOperation("删除课程教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void editCourseTeacher(@PathVariable Long courseId, @PathVariable Long id){

        courseTeacherService.deleteCourseTeacher(courseId, id);
    }

    @GetMapping("/teacher/compareWithLastYear")
    public ResultResponse<CompareWithLastYear> compare(){
        return courseTeacherService.compareWithLastYear();
    }
}
