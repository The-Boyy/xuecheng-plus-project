//package com.xuecheng.content;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import org.apache.commons.lang3.StringUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import com.xuecheng.base.model.PageParams;
//import com.xuecheng.base.model.PageResult;
//import com.xuecheng.content.mapper.CourseBaseMapper;
//import com.xuecheng.content.model.po.CourseBase;
//import com.xuecheng.content.model.dto.QueryCourseParamsDto;
//
//import java.util.List;
//
//@SpringBootTest
//public class CourseBaseMapperTests {
//
//    @Autowired
//    CourseBaseMapper courseBaseMapper;
//
//    @Test
//    public void testCourseBaseMapper(){
//
//        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
//        courseParamsDto.setCourseName("java");
//
//        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
//
//        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
//
//        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
//
//        PageParams pageParams = new PageParams();
//        pageParams.setPageNo(1L);
//        pageParams.setPageSize(2L);
//
//        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
//
//        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
//
//        List<CourseBase> items = pageResult.getRecords();
//        long total = pageResult.getTotal();
//
//        PageResult<CourseBase> courseBasePageResult = new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
//    }
//}
