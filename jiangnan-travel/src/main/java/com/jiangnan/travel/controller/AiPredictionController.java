package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.AiPredictionService;
import com.jiangnan.travel.vo.RecommendDestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiPredictionController {

    private final AiPredictionService aiPredictionService;

    @GetMapping("/recommend-dest")
    public Result<List<RecommendDestVO>> recommendDest(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(aiPredictionService.recommendDestinations(userId));
    }
}
