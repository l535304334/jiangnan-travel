package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_driver")
public class Driver extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String realName;
    private String idCard;
    private String driverLicense;
    private String carPlate;
    private Long carTypeId;
    private Integer status;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal avgRating;
    private Integer totalOrders;
    private Integer verifyStatus;
    private Integer onlineDuration;
}
