package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.vo.DriverVO;
import com.jiangnan.travel.vo.LoginVO;

import java.util.Map;

public interface DriverService {

    LoginVO login(String phone);

    void register(DriverRegisterRequest request);

    void updateStatus(Long driverId, Integer status);

    void updateLocation(Long driverId, java.math.BigDecimal lat, java.math.BigDecimal lng);

    DriverVO getProfile(Long driverId);

    Long getDriverIdByUserId(Long userId);

    Map<String, Object> getEarningStats(Long driverId);

    Map<String, Object> getWeeklyEarnings(Long driverId);

    Page<Driver> listDrivers(Integer verifyStatus, int page, int size);

    void verifyDriver(Long id, Integer verifyStatus);

    long countOnlineDrivers();
}
