package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_order_track")
public class OrderTrack {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String action;
    private Long operatorId;
    private String operatorType;
    private String remark;
    private LocalDateTime createTime;
}
