package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单状态变更事件 — 每次状态流转记录一条。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order_event")
public class OrderEvent extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String orderNo;

    /** 变更前状态码 */
    private Integer fromStatus;
    /** 变更后状态码 */
    private Integer toStatus;

    private Long operatorId;
    /** user / driver / system */
    private String operatorType;

    private String remark;
    private LocalDateTime eventTime;
}
