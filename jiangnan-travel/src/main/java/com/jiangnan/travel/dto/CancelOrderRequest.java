package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "取消订单请求")
public class CancelOrderRequest {

    @Schema(description = "取消原因", example = "等待时间过长")
    private String reason;
}
