package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_risk_alert")
public class RiskAlert {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private Long userId;
    private Long orderId;
    private Integer alertLevel;
    private String title;
    private String detail;
    private Integer handled;
    private String handleRemark;
    private LocalDateTime createTime;
}
