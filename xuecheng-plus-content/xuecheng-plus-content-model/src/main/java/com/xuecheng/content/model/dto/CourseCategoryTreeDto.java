package com.xuecheng.content.model.dto;


import lombok.Data;
import com.xuecheng.content.model.po.CourseCategory;

import java.io.Serializable;
import java.util.List;

@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    List<CourseCategoryTreeDto> childrenTreeNodes;
}
