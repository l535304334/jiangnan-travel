package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_schedule_route")
public class ScheduleRoute {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String startCity;
    private String endCity;
    private String departTime;
    private BigDecimal price;
    private Integer totalSeats;
    private Integer status;
    private LocalDateTime createTime;
}
