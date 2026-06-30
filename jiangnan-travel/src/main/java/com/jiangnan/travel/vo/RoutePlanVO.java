package com.jiangnan.travel.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoutePlanVO {
    private Integer distance;            // 路线总距离（米）
    private Integer duration;            // 路线总时长（秒）
    private Integer trafficLights;       // 红绿灯数量
    private Integer tolls;               // 收费站数量
    private List<RouteStepVO> steps;     // 分段指引
    private List<CoordVO> path;          // 路线坐标点（用于地图绘制）
    private String strategy;             // 规划策略
}
