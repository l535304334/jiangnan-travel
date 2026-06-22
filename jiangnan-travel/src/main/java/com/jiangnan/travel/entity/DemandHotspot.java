package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_demand_hotspot")
public class DemandHotspot {

    @TableId(type = IdType.AUTO)
    private Long id;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer radius;
    private Integer demandCount;
    private LocalDateTime snapshotTime;
    private LocalDateTime createTime;
}
