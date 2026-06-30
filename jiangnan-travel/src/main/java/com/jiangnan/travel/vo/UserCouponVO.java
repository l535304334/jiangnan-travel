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
@Schema(description = "用户优惠券信息")
public class UserCouponVO {

    @Schema(description = "用户优惠券ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "优惠券模板ID", example = "1")
    private Long couponId;

    @Schema(description = "状态 0=未使用 1=已使用", example = "0")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "关联订单ID")
    private Long useOrderId;

    @Schema(description = "优惠券名称", example = "新用户专享")
    private String name;

    @Schema(description = "使用门槛", example = "10")
    private BigDecimal threshold;

    @Schema(description = "优惠金额", example = "5")
    private BigDecimal discount;

    @Schema(description = "有效期天数", example = "30")
    private Integer validDays;
}
