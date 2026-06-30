package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI数据分析请求")
public class AiInsightRequest {

    @Schema(description = "分析类型：overview-概览, order-订单, revenue-营收", example = "overview")
    private String type;
}
