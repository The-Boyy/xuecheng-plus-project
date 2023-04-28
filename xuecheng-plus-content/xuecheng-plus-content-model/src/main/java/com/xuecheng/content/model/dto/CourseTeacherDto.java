package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value="CourseTeacherDto", description="课程教师基本信息")
public class CourseTeacherDto extends CourseTeacher {
}
