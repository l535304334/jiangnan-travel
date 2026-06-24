package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.AiPredictionService;
import com.jiangnan.travel.vo.RecommendDestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI预测", description = "AI智能目的地推荐")
public class AiPredictionController {

    private final AiPredictionService aiPredictionService;

    @GetMapping("/recommend-dest")
    @Operation(summary = "推荐目的地", description = "AI推荐热门出行目的地")
    public Result<List<RecommendDestVO>> recommendDest(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(aiPredictionService.recommendDestinations(userId));
    }
}
