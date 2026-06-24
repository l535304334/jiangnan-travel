package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_coupon")
public class Coupon extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal threshold;
    private BigDecimal discount;
    private Integer validDays;
    private Integer status;
}
