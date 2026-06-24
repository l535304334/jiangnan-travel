package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.CityLandmark;
import com.jiangnan.travel.service.CityLandmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landmark")
@RequiredArgsConstructor
@Tag(name = "城市地标", description = "城市文旅地标查询")
public class CityLandmarkController {

    private final CityLandmarkService cityLandmarkService;

    @GetMapping
    @Operation(summary = "地标列表", description = "获取城市文旅地标列表")
    public Result<List<CityLandmark>> listAll() {
        return Result.ok(cityLandmarkService.listAll());
    }

    @GetMapping("/search")
    public Result<List<CityLandmark>> search(@RequestParam String keyword) {
        return Result.ok(cityLandmarkService.search(keyword));
    }
}
