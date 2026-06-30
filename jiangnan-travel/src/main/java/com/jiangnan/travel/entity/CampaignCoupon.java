package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_campaign_coupon")
public class CampaignCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long campaignId;
    private Long couponId;
    private LocalDateTime createTime;
}
