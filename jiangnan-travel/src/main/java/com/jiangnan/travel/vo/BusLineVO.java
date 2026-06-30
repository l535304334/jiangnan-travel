package com.jiangnan.travel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "班线信息")
public class BusLineVO {

    @Schema(description = "班线ID", example = "1")
    private Long id;

    @Schema(description = "班线名称", example = "南昌→九江")
    private String lineName;

    @Schema(description = "出发城市", example = "南昌")
    private String startCity;

    @Schema(description = "到达城市", example = "九江")
    private String endCity;

    @Schema(description = "班线类型", example = "regular")
    private String busType;

    @Schema(description = "类型名称", example = "豪华大巴")
    private String busTypeName;

    @Schema(description = "时长(分钟)", example = "120")
    private Integer duration;

    @Schema(description = "票价", example = "65.00")
    private BigDecimal price;

    @Schema(description = "里程(公里)", example = "135")
    private Integer distance;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "时刻表")
    private List<ScheduleVO> schedules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "班次信息")
    public static class ScheduleVO {

        @Schema(description = "班次ID", example = "1")
        private Long id;

        @Schema(description = "发车时间", example = "08:00")
        private String departTime;

        @Schema(description = "到达时间", example = "10:00")
        private String arriveTime;

        @Schema(description = "总票数", example = "45")
        private Integer ticketCount;

        @Schema(description = "余票", example = "32")
        private Integer remaining;

        @Schema(description = "状态", example = "1")
        private Integer status;
    }
}
