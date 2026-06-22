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
public class DriverVO {

    private Long id;
    private String realName;
    private String carPlate;
    private String carTypeName;
    private Integer status;
    private String statusText;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal avgRating;
    private Integer totalOrders;
    private Integer verifyStatus;
    private Integer onlineDuration;
    private LocalDateTime createTime;
}
