package com.jiangnan.travel.vo;

import com.jiangnan.travel.entity.Campaign;
import com.jiangnan.travel.entity.Coupon;
import lombok.Data;

import java.util.List;

@Data
public class CampaignDetailVO {
    private Campaign campaign;
    private List<Coupon> coupons;
}
