package com.xuecheng.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.system.model.dto.ManagementDto;
import com.xuecheng.system.model.po.Management;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ManagementMapper extends BaseMapper<Management> {

    public List<ManagementDto> queryAll();
}
