package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal threshold;
    private BigDecimal discount;
    private Integer validDays;
    private Integer status;
    private LocalDateTime createTime;
}
