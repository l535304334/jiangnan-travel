package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_bus_line")
public class BusLine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String lineName;
    private String startCity;
    private String endCity;
    private String busType;
    private Integer duration;
    private BigDecimal price;
    private Integer distance;
    private Integer status;
    private LocalDateTime createTime;
}
