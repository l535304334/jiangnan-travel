package com.jiangnan.travel.service;

import com.jiangnan.travel.vo.RecommendDestVO;

import java.util.List;

public interface AiPredictionService {

    List<RecommendDestVO> recommendDestinations(Long userId);
}
