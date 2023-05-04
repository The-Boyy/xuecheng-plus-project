package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CategoryWithCountDto;
import com.xuecheng.content.model.po.CourseBase;

import java.util.List;

/**
 * <p>
 * 课程基本信息 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseBaseMapper extends BaseMapper<CourseBase> {

    public List<CategoryWithCountDto> queryCategoryWithCount();

}
