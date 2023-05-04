package com.xuecheng.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.system.mapper.DictionaryMapper;
import com.xuecheng.system.mapper.XcLogMapper;
import com.xuecheng.system.model.dto.XcLogDto;
import com.xuecheng.system.model.po.Dictionary;
import com.xuecheng.system.model.po.XcLog;
import com.xuecheng.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {

    @Autowired
    XcLogMapper xcLogMapper;

    @Override
    public List<Dictionary> queryAll() {

        List<Dictionary> list = this.list();


        return list;
    }

    @Override
    public Dictionary getByCode(String code) {


        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getCode, code);

        Dictionary dictionary = this.getOne(queryWrapper);


        return dictionary;
    }

    @Override
    public ResultResponse<List<XcLogDto>> queryLogs() {

        LambdaQueryWrapper<XcLog> wrapper = new LambdaQueryWrapper<XcLog>().orderByDesc(XcLog::getCreateTime);

        List<XcLog> xcLogs = xcLogMapper.selectList(wrapper);

        List<XcLogDto> xcLogDtoList = new ArrayList<>();
        for (XcLog xcLog : xcLogs) {
            XcLogDto xcLogDto = new XcLogDto();
            BeanUtils.copyProperties(xcLog, xcLogDto);
            xcLogDtoList.add(xcLogDto);
        }

        return ResultResponse.success(200, xcLogDtoList);
    }
}
