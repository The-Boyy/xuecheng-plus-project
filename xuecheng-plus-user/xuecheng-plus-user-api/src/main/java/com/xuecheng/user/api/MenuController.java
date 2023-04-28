package com.xuecheng.user.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.ResultResponse;
import com.xuecheng.user.model.dto.MenuResultDto;
import com.xuecheng.user.model.dto.XcUserDto;
import com.xuecheng.user.service.MenuService;
import com.xuecheng.user.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@PreAuthorize("hasAuthority('super_admin')")
public class MenuController {

    @Autowired
    MenuService menuService;

    @GetMapping("/menu/list")
    public ResultResponse<MenuResultDto> queryList(){

        SecurityUtil.XcUser user = SecurityUtil.getUser();

        if(user == null){
            XueChengPlusException.cast("请登录");
        }
        Long userId = Long.parseLong(user.getId());

        MenuResultDto menuResultDto = menuService.queryList(userId);

        XcUserDto xcUserDto = new XcUserDto();

        xcUserDto.setQq(user.getQq());
        xcUserDto.setBirthday(user.getBirthday());
        xcUserDto.setEmail(user.getEmail());
        xcUserDto.setNickname(user.getNickname());
        xcUserDto.setCellphone(user.getCellphone());
        xcUserDto.setUsername(user.getUsername());
        xcUserDto.setUserpic(user.getUserpic());

        menuResultDto.setXcUserDto(xcUserDto);

        return ResultResponse.success(200, menuResultDto);
    }
}
