package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment")
public class Payment extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String payMethod;
    private String payNo;
    private Integer status;
    private LocalDateTime payTime;
}
