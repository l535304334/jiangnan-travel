package com.jiangnan.travel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDestVO {

    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer orderCount;
    private String timeSlot;
}
