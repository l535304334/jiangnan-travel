package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order_track")
public class OrderTrack extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String action;
    private Long operatorId;
    private String operatorType;
    private String remark;
}
