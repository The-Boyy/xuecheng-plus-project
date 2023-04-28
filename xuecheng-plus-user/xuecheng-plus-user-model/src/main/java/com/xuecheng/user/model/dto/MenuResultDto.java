package com.xuecheng.user.model.dto;

import com.xuecheng.user.model.po.Router;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuResultDto {

    private List<ManagementDto> managementDtoList;

    private List<RouterDto> routerList;

    private XcUserDto xcUserDto;
}
