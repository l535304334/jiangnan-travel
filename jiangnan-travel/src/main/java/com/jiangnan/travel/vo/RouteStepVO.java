package com.jiangnan.travel.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RouteStepVO {
    private String instruction;          // 行驶指引文字（如"沿井冈山大道行驶2.3公里"）
    private String road;                 // 道路名称
    private Integer distance;            // 本段距离（米）
    private Integer duration;            // 本段时长（秒）
    private Integer trafficLights;       // 本段红绿灯数
    private List<CoordVO> path;          // 本段坐标点
}
