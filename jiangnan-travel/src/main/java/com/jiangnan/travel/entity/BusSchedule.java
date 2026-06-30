package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("t_bus_schedule")
public class BusSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lineId;
    private LocalTime departTime;
    private LocalTime arriveTime;
    private Integer ticketCount;
    private Integer remaining;
    private Integer status;
    private LocalDateTime createTime;
}
