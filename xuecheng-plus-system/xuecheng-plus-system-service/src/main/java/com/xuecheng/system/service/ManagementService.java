package com.xuecheng.system.service;

import com.xuecheng.system.model.dto.ManagementDto;
import com.xuecheng.system.model.po.Management;

import java.util.List;

public interface ManagementService {

    List<ManagementDto> queryAll();
}
