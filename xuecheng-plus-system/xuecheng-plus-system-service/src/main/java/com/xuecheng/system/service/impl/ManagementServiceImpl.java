package com.xuecheng.system.service.impl;

import com.xuecheng.system.mapper.ManagementMapper;
import com.xuecheng.system.model.dto.ManagementDto;
import com.xuecheng.system.model.po.Management;
import com.xuecheng.system.service.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagementServiceImpl implements ManagementService {

    @Autowired
    ManagementMapper managementMapper;
    @Override
    public List<ManagementDto> queryAll() {
        return managementMapper.queryAll();
    }
}
