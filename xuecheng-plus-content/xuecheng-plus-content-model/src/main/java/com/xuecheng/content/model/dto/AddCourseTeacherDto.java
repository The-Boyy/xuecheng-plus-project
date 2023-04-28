package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value="AddCourseTeacherDto", description="新增教师基本信息")
public class AddCourseTeacherDto {

    private Long courseId;
    private String teacherName;
    private String position;
    private String introduction;
}
