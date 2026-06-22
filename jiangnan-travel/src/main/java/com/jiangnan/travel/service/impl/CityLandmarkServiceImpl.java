package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.entity.CityLandmark;
import com.jiangnan.travel.mapper.CityLandmarkMapper;
import com.jiangnan.travel.service.CityLandmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityLandmarkServiceImpl implements CityLandmarkService {

    private final CityLandmarkMapper cityLandmarkMapper;

    @Override
    public List<CityLandmark> listAll() {
        return cityLandmarkMapper.selectList(
                new LambdaQueryWrapper<CityLandmark>()
                        .eq(CityLandmark::getStatus, 1)
                        .orderByAsc(CityLandmark::getSort));
    }

    @Override
    public List<CityLandmark> search(String keyword) {
        return cityLandmarkMapper.selectList(
                new LambdaQueryWrapper<CityLandmark>()
                        .eq(CityLandmark::getStatus, 1)
                        .and(w -> w.like(CityLandmark::getName, keyword)
                                .or().like(CityLandmark::getCity, keyword))
                        .orderByAsc(CityLandmark::getSort));
    }
}
