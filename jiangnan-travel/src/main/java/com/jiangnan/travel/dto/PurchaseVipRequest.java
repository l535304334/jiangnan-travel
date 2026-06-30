package com.jiangnan.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseVipRequest {

    @NotNull(message = "VIP等级ID不能为空")
    private Long levelId;

    @NotNull(message = "付费类型不能为空")
    private Integer feeType; // 0月费 1年费
}
