package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_risk_profile")
public class UserRiskProfile extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer cancelCount;
    private Integer orderCount;
    private Integer complaintCount;
    private Integer riskScore;
    private Integer riskLevel;
}
