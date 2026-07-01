package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.DriverRegisterRequest;
import com.jiangnan.travel.entity.CarType;
import com.jiangnan.travel.entity.Driver;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.CarTypeMapper;
import com.jiangnan.travel.mapper.DriverMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.service.DriverService;
import com.jiangnan.travel.vo.DriverVO;
import com.jiangnan.travel.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverMapper driverMapper;
    private final UserMapper userMapper;
    private final CarTypeMapper carTypeMapper;
    private final OrderMapper orderMapper;
    private final JwtUtil jwtUtil;
    // ponytail: local cache for userId→driverId, avoids DB query on every driver API call
    private final ConcurrentHashMap<Long, Long> driverIdCache = new ConcurrentHashMap<>();

    @Override
    public LoginVO login(String phone) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        Driver driver = driverMapper.selectOne(
                new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, user.getId()));
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        if (driver.getVerifyStatus() == 0) throw new BusinessException(ErrorCode.DRIVER_NOT_VERIFIED);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), "DRIVER");
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

    @Override
    public Long getDriverIdByUserId(Long userId) {
        return driverIdCache.computeIfAbsent(userId, uid -> {
            Driver driver = driverMapper.selectOne(
                    new LambdaQueryWrapper<Driver>().eq(Driver::getUserId, uid));
            if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
            return driver.getId();
        });
    }

    @Override
    public Map<String, Object> getEarningStats(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);

        List<Order> todayCompleted = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDriverId, driverId)
                        .eq(Order::getStatus, 4)
                        .apply("DATE(create_time) = CURDATE()"));

        long todayOrders = todayCompleted.size();
        BigDecimal todayEarnings = todayCompleted.stream()
                .map(o -> o.getFinalPrice() != null ? o.getFinalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("todayOrders", todayOrders);
        result.put("todayEarnings", todayEarnings);
        result.put("onlineMinutes", driver.getOnlineDuration() != null ? driver.getOnlineDuration() : 0);
        result.put("totalOrders", driver.getTotalOrders() != null ? driver.getTotalOrders() : 0);
        result.put("avgRating", driver.getAvgRating() != null ? driver.getAvgRating() : BigDecimal.ZERO);
        return result;
    }

    @Override
    public Map<String, Object> getWeeklyEarnings(Long driverId) {
        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);

        Map<String, Object> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        BigDecimal weekTotal = BigDecimal.ZERO;
        int weekOrders = 0;

        // 查询本周完成的订单
        List<Order> weekOrdersList = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDriverId, driverId)
                        .eq(Order::getStatus, 4)
                        .ge(Order::getCreateTime, weekStart.atStartOfDay())
                        .le(Order::getCreateTime, today.plusDays(1).atStartOfDay()));
        for (Order o : weekOrdersList) {
            if (o.getFinalPrice() != null) {
                weekTotal = weekTotal.add(o.getFinalPrice());
            }
        }
        weekOrders = weekOrdersList.size();

        result.put("weekTotal", weekTotal);
        result.put("weekOrders", weekOrders);
        result.put("totalOrders", driver.getTotalOrders() != null ? driver.getTotalOrders() : 0);
        result.put("avgRating", driver.getAvgRating() != null ? driver.getAvgRating() : BigDecimal.ZERO);
        return result;
    }

    @Override
    public Page<Driver> listDrivers(Integer verifyStatus, int page, int size) {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        if (verifyStatus != null) {
            wrapper.eq(Driver::getVerifyStatus, verifyStatus);
        }
        wrapper.orderByDesc(Driver::getCreateTime);
        return driverMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void verifyDriver(Long id, Integer verifyStatus) {
        Driver driver = driverMapper.selectById(id);
        if (driver == null) throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
        driver.setVerifyStatus(verifyStatus);
        driverMapper.updateById(driver);
    }

    @Override
    public long countOnlineDrivers() {
        return driverMapper.selectCount(
                new LambdaQueryWrapper<Driver>().eq(Driver::getStatus, 1));
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
