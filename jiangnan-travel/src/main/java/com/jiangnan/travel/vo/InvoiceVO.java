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
@Schema(description = "发票信息")
public class InvoiceVO {

    @Schema(description = "发票ID", example = "1")
    private Long id;

    @Schema(description = "订单ID", example = "1")
    private Long orderId;

    @Schema(description = "订单编号", example = "JN20240101001")
    private String orderNo;

    @Schema(description = "发票编号", example = "INV20240101001")
    private String invoiceNo;

    @Schema(description = "发票抬头", example = "江南科技有限公司")
    private String title;

    @Schema(description = "税号", example = "91360100309683294U")
    private String taxNo;

    @Schema(description = "发票金额", example = "35.68")
    private BigDecimal amount;

    @Schema(description = "发票状态", example = "0")
    private Integer status;

    @Schema(description = "状态说明", example = "申请中")
    private String statusText;

    @Schema(description = "申请时间", example = "2024-01-01 10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "起点地址", example = "江南大学东门")
    private String startAddress;

    @Schema(description = "终点地址", example = "无锡火车站")
    private String endAddress;
}
