package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long driverId;
    private Long carTypeId;
    private String startAddress;
    private BigDecimal startLat;
    private BigDecimal startLng;
    private String endAddress;
    private BigDecimal endLat;
    private BigDecimal endLng;
    private Integer distance;
    private Integer duration;
    private BigDecimal basePrice;
    private BigDecimal surgeFactor;
    private BigDecimal couponDiscount;
    private BigDecimal finalPrice;
    private BigDecimal tollFee;
    private Integer status;
    private String cancelReason;
    private Integer isSafetyShare;
    private LocalDateTime createTime;
    private LocalDateTime acceptTime;
    private LocalDateTime arriveTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime cancelTime;
    private LocalDateTime updateTime;
}
