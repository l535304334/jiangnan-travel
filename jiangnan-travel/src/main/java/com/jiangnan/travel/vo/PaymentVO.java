package com.jiangnan.travel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付信息")
public class PaymentVO {

    @Schema(description = "支付ID", example = "1")
    private Long id;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单编号", example = "JN20240101001")
    private String orderNo;

    @Schema(description = "支付金额", example = "35.68")
    private BigDecimal amount;

    @Schema(description = "支付方式", example = "wxpay")
    private String payMethod;

    @Schema(description = "支付方式名称", example = "微信支付")
    private String payMethodName;

    @Schema(description = "支付流水号", example = "P20240101001")
    private String payNo;

    @Schema(description = "支付状态", example = "1")
    private Integer status;

    @Schema(description = "状态说明", example = "支付成功")
    private String statusText;

    @Schema(description = "支付时间", example = "2024-01-01 10:30:00")
    private LocalDateTime payTime;

    @Schema(description = "起点地址", example = "江南大学东门")
    private String startAddress;

    @Schema(description = "终点地址", example = "无锡火车站")
    private String endAddress;
}
