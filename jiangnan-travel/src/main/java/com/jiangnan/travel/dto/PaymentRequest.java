package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建支付请求")
public class PaymentRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "支付方式", example = "wechat")
    private String payMethod;

    @Schema(description = "幂等键（防重复支付）", example = "uuid-xxxx")
    private String idempotentKey;
}
