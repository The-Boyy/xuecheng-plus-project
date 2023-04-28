package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

@Data
@ToString
public class TeachplanDto extends Teachplan {

    private TeachplanMedia teachplanMedia;

    private List<TeachplanDto> teachPlanTreeNodes;
}
