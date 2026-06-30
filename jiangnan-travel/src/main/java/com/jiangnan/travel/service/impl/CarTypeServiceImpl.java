package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.UpdateCarTypeRequest;
import com.jiangnan.travel.entity.CarType;
import com.jiangnan.travel.mapper.CarTypeMapper;
import com.jiangnan.travel.service.CarTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarTypeServiceImpl implements CarTypeService {

    private final CarTypeMapper carTypeMapper;

    @Override
    public List<CarType> listAll() {
        return carTypeMapper.selectList(new LambdaQueryWrapper<CarType>().orderByAsc(CarType::getId));
    }

    @Override
    public void update(Long id, UpdateCarTypeRequest request) {
        CarType carType = carTypeMapper.selectById(id);
        if (carType == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getName() != null) {
            carType.setName(request.getName());
        }
        if (request.getBasePrice() != null) {
            carType.setBasePrice(request.getBasePrice());
        }
        if (request.getMidPerKm() != null) {
            carType.setMidPerKm(request.getMidPerKm());
        }
        if (request.getLongPerKm() != null) {
            carType.setLongPerKm(request.getLongPerKm());
        }
        if (request.getSuperLongPerKm() != null) {
            carType.setSuperLongPerKm(request.getSuperLongPerKm());
        }
        if (request.getPerMinPrice() != null) {
            carType.setPerMinPrice(request.getPerMinPrice());
        }
        if (request.getMaxPassengers() != null) {
            carType.setMaxPassengers(request.getMaxPassengers());
        }
        if (request.getStatus() != null) {
            carType.setStatus(request.getStatus());
        }
        carTypeMapper.updateById(carType);
    }
}
