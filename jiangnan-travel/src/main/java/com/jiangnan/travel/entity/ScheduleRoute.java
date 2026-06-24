package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_schedule_route")
public class ScheduleRoute extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String startCity;
    private String endCity;
    private String departTime;
    private BigDecimal price;
    private Integer totalSeats;
    private Integer status;
}
