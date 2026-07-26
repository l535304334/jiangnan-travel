package com.jiangnan.travel;

import com.jiangnan.travel.entity.CityLandmark;
import com.jiangnan.travel.entity.CityQuote;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.CityLandmarkMapper;
import com.jiangnan.travel.mapper.CityQuoteMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.impl.TravelInsightServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TravelInsightService 纯单元测试 — 不依赖数据库。
 * 覆盖：热点按订单热度排序、5km 半径判定、常走路线聚合排序、空数据兜底。
 */
@ExtendWith(MockitoExtension.class)
class TravelInsightServiceTest {

    @Mock
    private CityQuoteMapper cityQuoteMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CityLandmarkMapper cityLandmarkMapper;

    @InjectMocks
    private TravelInsightServiceImpl service;

    private CityLandmark landmark(String name, double lat, double lng) {
        CityLandmark l = new CityLandmark();
        l.setName(name);
        l.setCity("南昌");
        l.setLat(BigDecimal.valueOf(lat));
        l.setLng(BigDecimal.valueOf(lng));
        return l;
    }

    private Order orderEndingAt(double lat, double lng) {
        Order o = new Order();
        o.setEndLat(BigDecimal.valueOf(lat));
        o.setEndLng(BigDecimal.valueOf(lng));
        return o;
    }

    private Order orderRoute(String start, String end) {
        Order o = new Order();
        o.setStartAddress(start);
        o.setEndAddress(end);
        return o;
    }

    @Test
    @DisplayName("热门景点 — 按 5km 内完单数降序排序")
    void hotspots_sortedByNearbyOrderCount() {
        // 两地标相距约 9km，互不落入对方 5km 半径，避免边界歧义
        CityLandmark tengwang = landmark("滕王阁", 28.6829, 115.8580);
        CityLandmark qingyunpu = landmark("青云谱", 28.6200, 115.9250);
        when(cityLandmarkMapper.selectList(any())).thenReturn(List.of(qingyunpu, tengwang));
        // 2 单落在滕王阁 1km 内，1 单落在青云谱旁，1 单在 100km 外不计入任何热点
        when(orderMapper.selectList(any())).thenReturn(List.of(
                orderEndingAt(28.6850, 115.8600),
                orderEndingAt(28.6800, 115.8550),
                orderEndingAt(28.6210, 115.9260),
                orderEndingAt(29.7000, 116.9000)));

        List<Map<String, Object>> spots = service.hotspots();

        assertThat(spots).hasSize(2);
        assertThat(spots.get(0).get("name")).isEqualTo("滕王阁");
        assertThat(spots.get(0).get("count")).isEqualTo(2);
        assertThat(spots.get(1).get("name")).isEqualTo("青云谱");
        assertThat(spots.get(1).get("count")).isEqualTo(1);
    }

    @Test
    @DisplayName("热门景点 — 无地标时返回空列表且不查订单")
    void hotspots_emptyLandmarks_returnsEmpty() {
        when(cityLandmarkMapper.selectList(any())).thenReturn(List.of());

        assertThat(service.hotspots()).isEmpty();
    }

    @Test
    @DisplayName("热门景点 — 坐标缺失的地标热度为 0 而不是抛异常")
    void hotspots_landmarkWithoutCoords_countsZero() {
        CityLandmark noCoords = new CityLandmark();
        noCoords.setName("未标坐标");
        when(cityLandmarkMapper.selectList(any())).thenReturn(List.of(noCoords));
        when(orderMapper.selectList(any())).thenReturn(List.of(orderEndingAt(28.68, 115.86)));

        List<Map<String, Object>> spots = service.hotspots();

        assertThat(spots).hasSize(1);
        assertThat(spots.get(0).get("count")).isEqualTo(0);
    }

    @Test
    @DisplayName("常走路线 — 聚合计数并按次数取 Top2")
    void frequentRoutes_aggregatesAndLimitsTop2() {
        when(orderMapper.selectList(any())).thenReturn(List.of(
                orderRoute("家", "公司"),
                orderRoute("家", "公司"),
                orderRoute("家", "公司"),
                orderRoute("公司", "健身房"),
                orderRoute("公司", "健身房"),
                orderRoute("家", "机场"),
                orderRoute(null, "丢起点跳过")));

        List<Map<String, Object>> routes = service.frequentRoutes(1L);

        assertThat(routes).hasSize(2);
        assertThat(routes.get(0).get("startAddress")).isEqualTo("家");
        assertThat(routes.get(0).get("endAddress")).isEqualTo("公司");
        assertThat(routes.get(0).get("count")).isEqualTo(3);
        assertThat(routes.get(1).get("count")).isEqualTo(2);
    }

    @Test
    @DisplayName("城市短句 — 透传 mapper 查询结果")
    void cityQuotes_delegatesToMapper() {
        CityQuote quote = new CityQuote();
        quote.setContent("落霞与孤鹜齐飞");
        when(cityQuoteMapper.selectList(any())).thenReturn(List.of(quote));

        List<CityQuote> quotes = service.cityQuotes();

        assertThat(quotes).hasSize(1);
        assertThat(quotes.get(0).getContent()).isEqualTo("落霞与孤鹜齐飞");
    }
}
