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
@Schema(description = "司机信息")
public class DriverVO {

    @Schema(description = "司机ID", example = "1")
    private Long id;
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    @Schema(description = "车牌号", example = "赣A·88888")
    private String carPlate;
    @Schema(description = "车型名称", example = "经济型")
    private String carTypeName;
    @Schema(description = "状态", example = "1")
    private Integer status;
    @Schema(description = "状态说明", example = "在线")
    private String statusText;
    @Schema(description = "纬度", example = "31.2304")
    private BigDecimal lat;
    @Schema(description = "经度", example = "121.4737")
    private BigDecimal lng;
    @Schema(description = "平均评分", example = "4.8")
    private BigDecimal avgRating;
    @Schema(description = "总订单数", example = "156")
    private Integer totalOrders;
    @Schema(description = "审核状态", example = "1")
    private Integer verifyStatus;
    @Schema(description = "在线时长(分钟)", example = "360")
    private Integer onlineDuration;
    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;
}
