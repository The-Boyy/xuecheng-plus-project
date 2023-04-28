package com.xuecheng.system.model.dto;

import com.xuecheng.system.model.po.Management;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class ManagementDto extends Management implements Serializable {

    private List<Management> managementList;
}
