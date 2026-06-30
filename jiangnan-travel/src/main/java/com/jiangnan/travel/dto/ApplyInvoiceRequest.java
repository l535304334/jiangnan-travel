package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "申请发票请求")
public class ApplyInvoiceRequest {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @NotBlank(message = "发票抬头不能为空")
    @Schema(description = "发票抬头", example = "江南科技有限公司")
    private String title;

    @Schema(description = "税号", example = "91360100309683294U")
    private String taxNo;
}
