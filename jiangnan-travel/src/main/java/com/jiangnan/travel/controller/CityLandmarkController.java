package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.CityLandmark;
import com.jiangnan.travel.service.CityLandmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landmark")
@RequiredArgsConstructor
public class CityLandmarkController {

    private final CityLandmarkService cityLandmarkService;

    @GetMapping
    public Result<List<CityLandmark>> listAll() {
        return Result.ok(cityLandmarkService.listAll());
    }

    @GetMapping("/search")
    public Result<List<CityLandmark>> search(@RequestParam String keyword) {
        return Result.ok(cityLandmarkService.search(keyword));
    }
}
