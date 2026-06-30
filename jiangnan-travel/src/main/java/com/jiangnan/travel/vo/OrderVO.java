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

    @Schema(description = "订单ID", example = "1")
    private Long id;
    @Schema(description = "订单编号", example = "JN20240101001")
    private String orderNo;
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    @Schema(description = "司机ID", example = "1")
    private Long driverId;
    @Schema(description = "司机姓名", example = "张师傅")
    private String driverName;
    @Schema(description = "车牌号", example = "赣A·88888")
    private String carPlate;

    @Schema(description = "起点地址", example = "江南大学东门")
    private String startAddress;
    @Schema(description = "起点纬度", example = "31.2304")
    private BigDecimal startLat;
    @Schema(description = "起点经度", example = "121.4737")
    private BigDecimal startLng;
    @Schema(description = "终点地址", example = "无锡火车站")
    private String endAddress;
    @Schema(description = "终点纬度", example = "31.2504")
    private BigDecimal endLat;
    @Schema(description = "终点经度", example = "121.4937")
    private BigDecimal endLng;

    @Schema(description = "距离(米)", example = "5000")
    private Integer distance;
    @Schema(description = "时长(秒)", example = "600")
    private Integer duration;

    @Schema(description = "基础价格", example = "14.60")
    private BigDecimal basePrice;
    @Schema(description = "动态加价系数", example = "1.20")
    private BigDecimal surgeFactor;
    @Schema(description = "优惠券抵扣", example = "2.00")
    private BigDecimal couponDiscount;
    @Schema(description = "最终价格", example = "16.60")
    private BigDecimal finalPrice;
    @Schema(description = "过路费", example = "0.00")
    private BigDecimal tollFee;

    @Schema(description = "订单状态", example = "0")
    private Integer status;
    @Schema(description = "状态说明", example = "待接单")
    private String statusText;
    @Schema(description = "取消原因", example = "行程变更")
    private String cancelReason;
    @Schema(description = "是否安全分享", example = "0")
    private Integer isSafetyShare;

    @Schema(description = "车型名称", example = "经济型")
    private String carTypeName;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;
    @Schema(description = "接单时间", example = "2024-01-01 10:05:00")
    private LocalDateTime acceptTime;
    @Schema(description = "到达时间", example = "2024-01-01 10:10:00")
    private LocalDateTime arriveTime;
    @Schema(description = "行程开始时间", example = "2024-01-01 10:15:00")
    private LocalDateTime startTime;
    @Schema(description = "行程结束时间", example = "2024-01-01 10:30:00")
    private LocalDateTime endTime;
    @Schema(description = "取消时间", example = "2024-01-01 10:20:00")
    private LocalDateTime cancelTime;
}
