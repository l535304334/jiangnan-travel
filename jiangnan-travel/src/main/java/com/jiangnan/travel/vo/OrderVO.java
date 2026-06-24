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
@Schema(description = "订单信息")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;
    @Schema(description = "订单编号")
    private String orderNo;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "司机ID")
    private Long driverId;
    @Schema(description = "司机姓名")
    private String driverName;
    @Schema(description = "车牌号")
    private String carPlate;

    @Schema(description = "起点地址")
    private String startAddress;
    @Schema(description = "起点纬度")
    private BigDecimal startLat;
    @Schema(description = "起点经度")
    private BigDecimal startLng;
    @Schema(description = "终点地址")
    private String endAddress;
    @Schema(description = "终点纬度")
    private BigDecimal endLat;
    @Schema(description = "终点经度")
    private BigDecimal endLng;

    @Schema(description = "距离(米)")
    private Integer distance;
    @Schema(description = "时长(秒)")
    private Integer duration;

    @Schema(description = "基础价格")
    private BigDecimal basePrice;
    @Schema(description = "动态加价系数")
    private BigDecimal surgeFactor;
    @Schema(description = "优惠券抵扣")
    private BigDecimal couponDiscount;
    @Schema(description = "最终价格")
    private BigDecimal finalPrice;
    @Schema(description = "过路费")
    private BigDecimal tollFee;

    @Schema(description = "订单状态")
    private Integer status;
    @Schema(description = "状态说明")
    private String statusText;
    @Schema(description = "取消原因")
    private String cancelReason;
    @Schema(description = "是否安全分享")
    private Integer isSafetyShare;

    @Schema(description = "车型名称")
    private String carTypeName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "接单时间")
    private LocalDateTime acceptTime;
    @Schema(description = "到达时间")
    private LocalDateTime arriveTime;
    @Schema(description = "行程开始时间")
    private LocalDateTime startTime;
    @Schema(description = "行程结束时间")
    private LocalDateTime endTime;
    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;
}
