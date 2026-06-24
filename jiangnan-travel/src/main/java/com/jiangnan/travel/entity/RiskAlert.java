package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_risk_alert")
public class RiskAlert extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private Long userId;
    private Long orderId;
    private Integer alertLevel;
    private String title;
    private String detail;
    private String alertDesc;
    private Integer handled;
    private String handleRemark;
}
