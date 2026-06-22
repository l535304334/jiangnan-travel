package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_car_type")
public class CarType extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal midPerKm;
    private BigDecimal longPerKm;
    private BigDecimal superLongPerKm;
    private BigDecimal perMinPrice;
    private Integer maxPassengers;
    private Integer status;
}
