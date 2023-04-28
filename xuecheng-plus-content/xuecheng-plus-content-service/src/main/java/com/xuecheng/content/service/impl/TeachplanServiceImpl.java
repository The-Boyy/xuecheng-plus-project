package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/14 12:11
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    private int getTeachplanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId = saveTeachplanDto.getId();
        if(teachplanId == null){
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            //确定排序字段，找到它的同级节点个数，排序字段就是个数加1
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, parentid) + 1;
            teachplan.setOrderby(teachplanCount);
            teachplan.setCreateDate(LocalDateTime.now());
            teachplan.setChangeDate(LocalDateTime.now());
            teachplan.setIsPreview("0");

            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数复制到teachplan
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }

    @Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();

        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));

        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    public void deleteTeachplanById(Long teachplanId) {

        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan == null){
            XueChengPlusException.cast("教学计划不存在");
        }

        Integer grade = teachplan.getGrade();
        if(grade == 1){
            LambdaQueryWrapper<Teachplan> eq = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getParentid, teachplanId);
            Integer count = teachplanMapper.selectCount(eq);
            if(count != 0){
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
            teachplanMapper.deleteById(teachplanId);
        }else {
            teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));
            teachplanMapper.deleteById(teachplanId);
        }
    }

    @Override
    public void deleteTeachplanByCourseId(Long courseId) {
        LambdaQueryWrapper<Teachplan> teachplanWrapper = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanWrapper);
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getCourseId, courseId));
    }

    @Transactional
    @Override
    public void teachplanMovedown(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan == null){
            XueChengPlusException.cast("教学计划不存在");
        }

        Integer orderby = teachplan.getOrderby();
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();

        LambdaQueryWrapper<Teachplan> eq = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId, courseId).eq(Teachplan::getOrderby, orderby + 1).eq(Teachplan::getParentid, parentid);

        Teachplan teachplanNext = teachplanMapper.selectOne(eq);
        if(teachplanNext == null){
            XueChengPlusException.cast("无法向下移动，已是最后一个");
        }
        teachplanNext.setOrderby(orderby);
        teachplan.setOrderby(orderby + 1);
        int i1 = teachplanMapper.updateById(teachplan);
        int i2 = teachplanMapper.updateById(teachplanNext);
        if(i1 <= 0 || i2 <= 0){
            XueChengPlusException.cast("移动失败");
        }
    }

    @Override
    public void teachplanMoveup(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan == null){
            XueChengPlusException.cast("教学计划不存在");
        }

        Integer orderby = teachplan.getOrderby();
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();

        LambdaQueryWrapper<Teachplan> eq = new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getCourseId, courseId).eq(Teachplan::getOrderby, orderby - 1).eq(Teachplan::getParentid, parentid);

        Teachplan teachplanNext = teachplanMapper.selectOne(eq);
        if(teachplanNext == null){
            XueChengPlusException.cast("无法向上移动，已是第一个");
        }
        teachplanNext.setOrderby(orderby);
        teachplan.setOrderby(orderby - 1);
        int i1 = teachplanMapper.updateById(teachplan);
        int i2 = teachplanMapper.updateById(teachplanNext);
        if(i1 <= 0 || i2 <= 0){
            XueChengPlusException.cast("移动失败");
        }
    }
}
