package com.xuecheng.user.model.dto;

import com.xuecheng.user.model.po.XcMenu;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class MenuDto extends XcMenu implements Serializable {

    private List<XcMenu> menuSonList;
}
