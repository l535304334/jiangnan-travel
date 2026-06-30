package com.jiangnan.travel.service;

import com.jiangnan.travel.vo.RoutePlanVO;

public interface AmapRouteService {
    /**
     * 调用高德驾车路径规划API，获取起终点间的路线详情
     * @param originLng 起点经度
     * @param originLat 起点纬度
     * @param destLng 终点经度
     * @param destLat 终点纬度
     * @return 路线规划结果（含坐标点、距离、时长、红绿灯数、分段指引）
     */
    RoutePlanVO planRoute(double originLng, double originLat, double destLng, double destLat);
}
