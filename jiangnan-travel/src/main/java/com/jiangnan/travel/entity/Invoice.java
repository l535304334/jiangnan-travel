package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_invoice")
public class Invoice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private String invoiceNo;
    private String title;
    private String taxNo;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createTime;
}
