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

    @Schema(description = "司机ID")
    private Long id;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "车牌号")
    private String carPlate;
    @Schema(description = "车型名称")
    private String carTypeName;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "状态说明")
    private String statusText;
    @Schema(description = "纬度")
    private BigDecimal lat;
    @Schema(description = "经度")
    private BigDecimal lng;
    @Schema(description = "平均评分")
    private BigDecimal avgRating;
    @Schema(description = "总订单数")
    private Integer totalOrders;
    @Schema(description = "审核状态")
    private Integer verifyStatus;
    @Schema(description = "在线时长(分钟)")
    private Integer onlineDuration;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
