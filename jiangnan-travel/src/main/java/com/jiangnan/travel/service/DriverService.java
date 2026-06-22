package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.vo.DriverVO;
import com.jiangnan.travel.vo.LoginVO;

public interface DriverService {

    LoginVO login(String phone);

    void register(DriverRegisterRequest request);

    void updateStatus(Long driverId, Integer status);

    void updateLocation(Long driverId, java.math.BigDecimal lat, java.math.BigDecimal lng);

    DriverVO getProfile(Long driverId);
}
