package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_vip_level")
public class VipLevel extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer level;
    private BigDecimal discount;
    private BigDecimal minSpend;
    private BigDecimal monthlyFee;
    private BigDecimal yearlyFee;
    private String icon;
    private Integer status;
}
