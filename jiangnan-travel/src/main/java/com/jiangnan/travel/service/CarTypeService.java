package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.UpdateCarTypeRequest;
import com.jiangnan.travel.entity.CarType;

import java.util.List;

public interface CarTypeService {
    List<CarType> listAll();

    void update(Long id, UpdateCarTypeRequest request);
}
