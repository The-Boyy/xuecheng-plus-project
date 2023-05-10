package com.xuecheng.user.model.dto;

import com.xuecheng.user.model.po.XcMenu;
import lombok.Data;

import java.util.List;

@Data
public class PermissionsDto extends XcMenu {

    private List<PermissionsDto> childrenTreeNodes;
}
