package com.xuecheng.user.model.dto;

import com.xuecheng.user.model.po.Router;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuResultDto {

    private List<MenuDto> menuDtoList;

    private RouterReturnDto routerReturnDto;

    private XcUserDto xcUserDto;
}
