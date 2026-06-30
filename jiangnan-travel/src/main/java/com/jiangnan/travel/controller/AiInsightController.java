package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.AiInsightRequest;
import com.jiangnan.travel.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
@Tag(name = "AI分析", description = "AI智能数据分析")
public class AiInsightController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping("/insight")
    @Operation(summary = "数据分析", description = "AI对业务数据进行分析")
    public Result<Map<String, Object>> insight(@RequestBody AiInsightRequest request) {
        return Result.ok(Map.of("insight", aiAnalysisService.analyze()));
    }
}
