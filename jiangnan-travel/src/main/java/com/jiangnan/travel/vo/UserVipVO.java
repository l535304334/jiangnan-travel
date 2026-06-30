package com.jiangnan.travel.vo;

import com.jiangnan.travel.entity.VipLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVipVO {
    private Long id;
    private Long userId;
    private VipLevel vipLevel;
    private Integer feeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long remainingDays;
}
