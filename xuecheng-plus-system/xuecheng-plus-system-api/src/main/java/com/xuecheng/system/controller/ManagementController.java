package com.xuecheng.system.controller;

import com.xuecheng.system.model.dto.ManagementDto;
import com.xuecheng.system.service.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("hasAuthority('super_admin')")
@RestController
public class ManagementController {

    @Autowired
    ManagementService managementService;

    @GetMapping("/management/all")
    public List<ManagementDto> queryAll(){
        return managementService.queryAll();
    }
}
