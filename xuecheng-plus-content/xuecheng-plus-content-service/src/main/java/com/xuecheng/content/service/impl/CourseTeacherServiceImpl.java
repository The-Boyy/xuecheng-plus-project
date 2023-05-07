package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.dto.CompareWithLastYear;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacherDto> getCourseTeachers(Long courseId) {

        LambdaQueryWrapper<CourseTeacher> courseTeacherWrapper = new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId);

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(courseTeacherWrapper);

        List<CourseTeacherDto> courseTeacherResult = new ArrayList<>();
        for (CourseTeacher courseTeacher : courseTeachers) {
            CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
            BeanUtils.copyProperties(courseTeacher, courseTeacherDto);
            courseTeacherResult.add(courseTeacherDto);
        }

        return courseTeacherResult;
    }

    @Transactional
    @Override
    public CourseTeacherDto addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto) {

        CourseTeacher courseTeacher = new CourseTeacherDto();
        BeanUtils.copyProperties(addCourseTeacherDto, courseTeacher);
        courseTeacher.setCreateDate(LocalDateTime.now());
        int insert = courseTeacherMapper.insert(courseTeacher);
        if(insert < 1){
            XueChengPlusException.cast("添加课程教师失败");
        }
        CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
        BeanUtils.copyProperties(courseTeacher, courseTeacherDto);

        return courseTeacherDto;
    }

    @Transactional
    @Override
    public CourseTeacherDto editCourseTeacher(CourseTeacherDto courseTeacherDto) {

        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(courseTeacherDto, courseTeacher);

        int i = courseTeacherMapper.updateById(courseTeacher);
        if(i < 1){
            XueChengPlusException.cast("修改课程教师信息失败");
        }
        return courseTeacherDto;
    }

    @Transactional
    @Override
    public void deleteCourseTeacher(Long courseId, Long id) {

        int i = courseTeacherMapper.deleteById(id);
        if(i < 1){
            XueChengPlusException.cast("删除课程教师失败");
        }
    }

    @Override
    public void deleteCourseTeachers(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> eq = new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(eq);
    }

    @Override
    public ResultResponse<CompareWithLastYear> compareWithLastYear() {
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<CourseTeacher>().orderByDesc(CourseTeacher::getCreateDate);

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(wrapper);

        Map<Integer, Integer> map = new HashMap<>();

        int year = LocalDateTime.now().getYear();
        map.put(year, 0);
        map.put(year - 1, 0);

        for (CourseTeacher courseTeacher : courseTeachers) {
            int y = courseTeacher.getCreateDate().getYear();
            if(map.containsKey(y)){
                map.put(y, map.get(y) + 1);
            }else {
                break;
            }
        }

        CompareWithLastYear compareWithLastYear = new CompareWithLastYear();
        compareWithLastYear.setDate(year + "年");

        Integer curNum = map.get(year);
        Integer lastNum = map.get(year - 1);
        compareWithLastYear.setFlag(curNum >= lastNum);

        compareWithLastYear.setRate((curNum - lastNum) / (lastNum + 0.000001) * 100);

        return ResultResponse.success(200, compareWithLastYear);
    }
}
