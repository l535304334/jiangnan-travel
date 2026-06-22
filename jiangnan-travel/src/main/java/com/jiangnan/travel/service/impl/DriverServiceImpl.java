package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.entity.CarType;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.CarTypeMapper;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.service.DriverService;
import com.jiangnan.travel.vo.DriverVO;
import com.jiangnan.travel.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverMapper driverMapper;
    private final UserMapper userMapper;
    private final CarTypeMapper carTypeMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(String phone) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        Driver driver = driverMapper.selectOne(
                new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, user.getId()));
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        if (driver.getVerifyStatus() == 0) throw new BusinessException(ErrorCode.DRIVER_NOT_VERIFIED);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return LoginVO.builder()
                .token(token).userId(user.getId())
                .phone(user.getPhone()).nickname(driver.getRealName()).build();
    }

    @Override
    public void register(DriverRegisterRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        Long count = driverMapper.selectCount(
                new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, user.getId()));
        if (count > 0) throw new BusinessException(3004, "您已是司机");

        Driver driver = new Driver();
        driver.setUserId(user.getId());
        driver.setRealName(request.getRealName());
        driver.setIdCard(request.getIdCard());
        driver.setDriverLicense(request.getDriverLicense());
        driver.setCarPlate(request.getCarPlate());
        driver.setCarTypeId(1L);
        driver.setStatus(0);
        driver.setVerifyStatus(0);
        driver.setAvgRating(BigDecimal.valueOf(4.00));
        driverMapper.insert(driver);
    }

    @Override
    public void updateStatus(Long driverId, Integer status) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        driver.setStatus(status);
        driverMapper.updateById(driver);
    }

    @Override
    public void updateLocation(Long driverId, BigDecimal lat, BigDecimal lng) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) return;
        driver.setLat(lat);
        driver.setLng(lng);
        driverMapper.updateById(driver);
    }

    @Override
    public DriverVO getProfile(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        return toVO(driver);
    }

    private DriverVO toVO(Driver d) {
        String carTypeName = "";
        if (d.getCarTypeId() != null) {
            CarType ct = carTypeMapper.selectById(d.getCarTypeId());
            if (ct != null) carTypeName = ct.getName();
        }
        return DriverVO.builder()
                .id(d.getId()).realName(d.getRealName())
                .carPlate(d.getCarPlate()).carTypeName(carTypeName)
                .status(d.getStatus())
                .statusText(switch (d.getStatus()) {
                    case 0 -> "离线"; case 1 -> "在线"; case 2 -> "忙碌";
                    case 3 -> "休息中"; default -> "未知";
                })
                .lat(d.getLat()).lng(d.getLng())
                .avgRating(d.getAvgRating()).totalOrders(d.getTotalOrders())
                .verifyStatus(d.getVerifyStatus()).onlineDuration(d.getOnlineDuration())
                .createTime(d.getCreateTime())
                .build();
    }
}
