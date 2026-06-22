package com.jiangnan.travel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private Long driverId;
    private String driverName;
    private String carPlate;

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
    private String statusText;
    private String cancelReason;
    private Integer isSafetyShare;

    private String carTypeName;

    private LocalDateTime createTime;
    private LocalDateTime acceptTime;
    private LocalDateTime arriveTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime cancelTime;
}
