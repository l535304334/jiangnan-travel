package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_demand_hotspot")
public class DemandHotspot extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer radius;
    private Integer demandCount;
    private LocalDateTime snapshotTime;
}
