package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_schedule_order")
public class ScheduleOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long routeId;
    private LocalDate departDate;
    private Integer seatCount;
    private BigDecimal totalPrice;
    private Integer status;
    private LocalDateTime createTime;
}
