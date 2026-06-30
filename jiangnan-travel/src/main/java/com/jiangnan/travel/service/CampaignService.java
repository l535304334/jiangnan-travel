package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.entity.Campaign;
import com.jiangnan.travel.vo.CampaignDetailVO;

import java.util.List;

public interface CampaignService {

    /** 用户端：当前可参与的活动列表 */
    List<Campaign> listActive();

    /** 用户端：活动详情（含关联优惠券） */
    CampaignDetailVO getDetail(Long campaignId);

    /** 管理端：活动分页列表 */
    Page<Campaign> listAdmin(String keyword, int page, int size);

    /** 管理端：创建活动 */
    void create(Campaign campaign, List<Long> couponIds);

    /** 管理端：更新活动 */
    void update(Campaign campaign, List<Long> couponIds);

    /** 管理端：删除活动 */
    void delete(Long id);

    /** 用户端：领取活动优惠券 */
    void claimCoupons(Long userId, Long campaignId);
}
