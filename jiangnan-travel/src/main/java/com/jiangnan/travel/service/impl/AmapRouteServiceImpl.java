package com.jiangnan.travel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangnan.travel.service.AmapRouteService;
import com.jiangnan.travel.vo.CoordVO;
import com.jiangnan.travel.vo.RoutePlanVO;
import com.jiangnan.travel.vo.RouteStepVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmapRouteServiceImpl implements AmapRouteService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${amap.web-api-key}")
    private String apiKey;

    @Value("${amap.base-url}")
    private String baseUrl;

    @Override
    public RoutePlanVO planRoute(double originLng, double originLat, double destLng, double destLat) {
        try {
            String origin = originLng + "," + originLat;
            String destination = destLng + "," + destLat;

            // 调用3种策略取最优：0=速度优先, 1=费用优先, 2=距离优先
            List<RoutePlanVO> candidates = new ArrayList<>();
            int[] strategies = {0, 1, 2};

            for (int strategy : strategies) {
                try {
                    String url = String.format(
                            "%s?key=%s&origin=%s&destination=%s&strategy=%d&extensions=all&cartype=0",
                            baseUrl, apiKey, origin, destination, strategy
                    );
                    log.info("高德路线规划[strategy={}]: origin={}, dest={}", strategy, origin, destination);
                    String response = restTemplate.getForObject(url, String.class);
                    RoutePlanVO plan = parseRouteResponse(response);
                    if (plan != null) candidates.add(plan);
                } catch (Exception e) {
                    log.warn("strategy={} 调用失败", strategy, e);
                }
            }

            if (!candidates.isEmpty()) {
                // 按综合评分选最优：分数 = distance*0.3 + duration*0.5 + trafficLights*0.2（越低越好）
                candidates.sort((a, b) -> {
                    double scoreA = a.getDistance() * 0.3 + a.getDuration() * 0.5 + a.getTrafficLights() * 0.2;
                    double scoreB = b.getDistance() * 0.3 + b.getDuration() * 0.5 + b.getTrafficLights() * 0.2;
                    return Double.compare(scoreA, scoreB);
                });
                return candidates.get(0);
            }

            throw new RuntimeException("所有策略均失败");
        } catch (Exception e) {
            log.error("高德驾车路径规划API调用失败，启用fallback模式", e);
            return fallbackRoute(originLng, originLat, destLng, destLat);
        }
    }

    /**
     * 解析高德驾车路径规划API返回的JSON
     */
    private RoutePlanVO parseRouteResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);

        String status = root.path("status").asText();
        if (!"1".equals(status)) {
            String info = root.path("info").asText();
            log.warn("高德API返回失败状态: status={}, info={}", status, info);
            throw new RuntimeException("高德API返回失败: " + info);
        }

        JsonNode route = root.path("route");
        JsonNode paths = route.path("paths");
        if (paths.isEmpty()) {
            throw new RuntimeException("高德API返回的路线为空");
        }

        JsonNode path0 = paths.get(0);
        int totalDistance = path0.path("distance").asInt(0);
        int totalDuration = path0.path("duration").asInt(0);
        String strategy = path0.path("strategy").asText("");
        int tolls = path0.path("tolls").asInt(0);

        // 提取实时路况信息
        int trafficCongestion = extractTrafficInfo(path0);

        JsonNode steps = path0.path("steps");
        int totalTrafficLights = 0;
        List<RouteStepVO> stepList = new ArrayList<>();
        List<CoordVO> allPath = new ArrayList<>();
        int stepNumber = 0;

        for (JsonNode stepNode : steps) {
            stepNumber++;
            String instruction = stepNode.path("instruction").asText("");
            String road = stepNode.path("road").asText("");
            int stepDistance = stepNode.path("distance").asInt(0);
            int stepDuration = stepNode.path("duration").asInt(0);
            int stepTrafficLights = stepNode.path("traffic_lights").asInt(0);

            totalTrafficLights += stepTrafficLights;

            // 解析坐标点 polyline: "lng,lat;lng,lat;..."
            List<CoordVO> stepPath = parsePolyline(stepNode.path("polyline").asText(""));

            // 提取红绿灯位置
            String trafficLights_desc = stepNode.path("traffic_lights_desc").asText("");

            stepList.add(RouteStepVO.builder()
                    .instruction(instruction)
                    .road(road)
                    .distance(stepDistance)
                    .duration(stepDuration)
                    .trafficLights(stepTrafficLights)
                    .path(stepPath)
                    .build());

            allPath.addAll(stepPath);
        }

        return RoutePlanVO.builder()
                .distance(totalDistance)
                .duration(totalDuration)
                .trafficLights(totalTrafficLights)
                .tolls(tolls)
                .steps(stepList)
                .path(allPath)
                .strategy(strategy)
                .build();
    }

    /**
     * 提取实时路况信息
     * 使用高德API返回的交通拥堵信息
     */
    private int extractTrafficInfo(JsonNode pathNode) {
        try {
            JsonNode traffic = pathNode.path("traffic_condition");
            if (traffic != null && !traffic.isEmpty()) {
                int totalCount = 0;
                for (JsonNode t : traffic) {
                    String status = t.path("status").asText("0");
                    if ("2".equals(status) || "3".equals(status) || "4".equals(status)) {
                        totalCount++;
                    }
                }
                return totalCount;
            }
        } catch (Exception e) {
            log.debug("提取路况信息失败（非关键异常）", e);
        }
        return 0;
    }

    /**
     * 解析高德polyline格式："lng,lat;lng,lat;..." -> List<CoordVO>
     */
    private List<CoordVO> parsePolyline(String polyline) {
        if (polyline == null || polyline.isEmpty()) {
            return Collections.emptyList();
        }
        List<CoordVO> coords = new ArrayList<>();
        String[] points = polyline.split(";");
        for (String point : points) {
            String[] parts = point.split(",");
            if (parts.length == 2) {
                try {
                    double lng = Double.parseDouble(parts[0]);
                    double lat = Double.parseDouble(parts[1]);
                    coords.add(new CoordVO(lng, lat));
                } catch (NumberFormatException e) {
                    log.warn("解析坐标点失败: {}", point);
                }
            }
        }
        return coords;
    }

    /**
     * fallback模式：基于直线距离估计的简单路线
     */
    private RoutePlanVO fallbackRoute(double originLng, double originLat, double destLng, double destLat) {
        log.info("启用fallback路线规划: ({}, {}) -> ({}, {})", originLng, originLat, destLng, destLat);

        // 使用球面余弦定律计算直线距离（米）
        double earthRadius = 6371000;
        double dLng = Math.toRadians(destLng - originLng);
        double dLat = Math.toRadians(destLat - originLat);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(originLat)) * Math.cos(Math.toRadians(destLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        int distance = (int) Math.round(earthRadius * c);

        // 假设平均时速30km/h
        int duration = (int) ((double) distance / 30000 * 3600);

        // 构造简单的起终点路径
        List<CoordVO> path = new ArrayList<>();
        path.add(new CoordVO(originLng, originLat));
        path.add(new CoordVO(destLng, destLat));

        RouteStepVO step = RouteStepVO.builder()
                .instruction(String.format("直线距离约%d米，预计行驶约%d分钟", distance, duration / 60))
                .road("")
                .distance(distance)
                .duration(duration)
                .trafficLights(0)
                .path(path)
                .build();

        return RoutePlanVO.builder()
                .distance(distance)
                .duration(duration)
                .trafficLights(0)
                .tolls(0)
                .steps(Collections.singletonList(step))
                .path(path)
                .strategy("直线距离（fallback）")
                .build();
    }
}
