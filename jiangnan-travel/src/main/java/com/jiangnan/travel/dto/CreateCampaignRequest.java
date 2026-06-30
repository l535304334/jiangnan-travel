package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "创建/更新活动请求")
public class CreateCampaignRequest {

    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "活动banner图URL")
    private String bannerUrl;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动类型 0通用 1新用户 2限时", example = "0")
    private Integer type;

    @Schema(description = "关联优惠券ID列表")
    private List<Long> couponIds;
}
