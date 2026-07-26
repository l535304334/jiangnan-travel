package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.CityQuote;

import java.util.List;
import java.util.Map;

/**
 * 文旅数据洞察 — 热门景点、文化短句、用户常走路线。
 */
public interface TravelInsightService {

    /** 热门景点：以城市地标为候选，按近 30 天完单终点热度排序。 */
    List<Map<String, Object>> hotspots();

    /** 城市文化短句（启用状态，按 sort 排序）。 */
    List<CityQuote> cityQuotes();

    /** 用户近 30 天常走路线 Top2。 */
    List<Map<String, Object>> frequentRoutes(Long userId);
}
