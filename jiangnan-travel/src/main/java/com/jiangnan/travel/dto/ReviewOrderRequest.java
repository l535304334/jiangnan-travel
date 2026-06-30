package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "评价订单请求")
public class ReviewOrderRequest {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    @Schema(description = "评分（1-5）", example = "5")
    private Integer rating;

    @Schema(description = "评价标签", example = "准时,服务好")
    private String tags;

    @Schema(description = "评价内容", example = "服务很好")
    private String content;
}
