package com.jiangnan.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.*;
import com.jiangnan.travel.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理后台", description = "管理后台用户、司机、订单、风控管理")
public class AdminManageController {

    private final UserMapper userMapper;
    private final DriverMapper driverMapper;
    private final OrderMapper orderMapper;
    private final RiskAlertMapper riskAlertMapper;
    private final CarTypeMapper carTypeMapper;

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                Authentication authentication) {
        Page<User> userPage = new Page<>(page, size);
        userPage = userMapper.selectPage(userPage, new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime));
        return Result.ok(userPage);
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id,
                                       @RequestBody Map<String, Object> body,
                                       Authentication authentication) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        Integer status = body.get("status") != null ? ((Number) body.get("status")).intValue() : null;
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            return Result.fail("无效的状态值");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok("状态更新成功");
    }

    @GetMapping("/drivers")
    @Operation(summary = "司机列表", description = "分页查询司机列表")
    public Result<?> listDrivers(@RequestParam(required = false) Integer verifyStatus,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  Authentication authentication) {
        Page<Driver> driverPage = new Page<>(page, size);
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<Driver>()
                .orderByDesc(Driver::getCreateTime);
        if (verifyStatus != null) {
            wrapper.eq(Driver::getVerifyStatus, verifyStatus);
        }
        driverPage = driverMapper.selectPage(driverPage, wrapper);
        return Result.ok(driverPage);
    }

    @PutMapping("/drivers/{id}/verify")
    public Result<?> verifyDriver(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body,
                                   Authentication authentication) {
        Driver driver = driverMapper.selectById(id);
        if (driver == null) {
            return Result.fail("司机不存在");
        }
        Integer verifyStatus = body.get("verifyStatus") != null ? ((Number) body.get("verifyStatus")).intValue() : null;
        if (verifyStatus == null || (verifyStatus != 1 && verifyStatus != 2)) {
            return Result.fail("无效的审核状态值");
        }
        driver.setVerifyStatus(verifyStatus);
        driverMapper.updateById(driver);
        return Result.ok("审核操作成功");
    }

    @GetMapping("/orders")
    public Result<?> listOrders(@RequestParam(required = false) Integer status,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 Authentication authentication) {
        Page<Order> orderPage = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        orderPage = orderMapper.selectPage(orderPage, wrapper);
        return Result.ok(orderPage);
    }

    @GetMapping("/alerts")
    @Operation(summary = "告警列表", description = "分页查询风控告警")
    public Result<?> listAlerts(@RequestParam(required = false) Integer handled,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 Authentication authentication) {
        Page<RiskAlert> alertPage = new Page<>(page, size);
        LambdaQueryWrapper<RiskAlert> wrapper = new LambdaQueryWrapper<RiskAlert>()
                .orderByDesc(RiskAlert::getCreateTime);
        if (handled != null) {
            wrapper.eq(RiskAlert::getHandled, handled);
        }
        alertPage = riskAlertMapper.selectPage(alertPage, wrapper);
        return Result.ok(alertPage);
    }

    @PutMapping("/alerts/{id}/handle")
    public Result<?> handleAlert(@PathVariable Long id,
                                  @RequestBody Map<String, Object> body,
                                  Authentication authentication) {
        RiskAlert alert = riskAlertMapper.selectById(id);
        if (alert == null) {
            return Result.fail("预警记录不存在");
        }
        alert.setHandled(1);
        String handleRemark = (String) body.get("handleRemark");
        if (handleRemark != null) {
            alert.setHandleRemark(handleRemark);
        }
        riskAlertMapper.updateById(alert);
        return Result.ok("预警处理成功");
    }

    @GetMapping("/car-types")
    @Operation(summary = "车型列表", description = "查询车型定价列表")
    public Result<?> listCarTypes(Authentication authentication) {
        List<CarType> carTypes = carTypeMapper.selectList(
                new LambdaQueryWrapper<CarType>().orderByAsc(CarType::getId));
        return Result.ok(carTypes);
    }

    @PutMapping("/car-types/{id}")
    public Result<?> updateCarType(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body,
                                    Authentication authentication) {
        CarType carType = carTypeMapper.selectById(id);
        if (carType == null) {
            return Result.fail("车型不存在");
        }
        if (body.containsKey("name")) {
            carType.setName((String) body.get("name"));
        }
        if (body.containsKey("basePrice")) {
            carType.setBasePrice(body.get("basePrice") != null
                    ? new BigDecimal(body.get("basePrice").toString()) : null);
        }
        if (body.containsKey("midPerKm")) {
            carType.setMidPerKm(body.get("midPerKm") != null
                    ? new BigDecimal(body.get("midPerKm").toString()) : null);
        }
        if (body.containsKey("longPerKm")) {
            carType.setLongPerKm(body.get("longPerKm") != null
                    ? new BigDecimal(body.get("longPerKm").toString()) : null);
        }
        if (body.containsKey("superLongPerKm")) {
            carType.setSuperLongPerKm(body.get("superLongPerKm") != null
                    ? new BigDecimal(body.get("superLongPerKm").toString()) : null);
        }
        if (body.containsKey("perMinPrice")) {
            carType.setPerMinPrice(body.get("perMinPrice") != null
                    ? new BigDecimal(body.get("perMinPrice").toString()) : null);
        }
        if (body.containsKey("maxPassengers")) {
            carType.setMaxPassengers(body.get("maxPassengers") != null
                    ? ((Number) body.get("maxPassengers")).intValue() : null);
        }
        if (body.containsKey("status")) {
            carType.setStatus(body.get("status") != null
                    ? ((Number) body.get("status")).intValue() : null);
        }
        carTypeMapper.updateById(carType);
        return Result.ok("车型更新成功");
    }

    @GetMapping("/dashboard")
    @Operation(summary = "数据大屏", description = "获取管理后台统计数据")
    public Result<?> dashboard(Authentication authentication) {
        long totalUsers = userMapper.selectCount(null);

        long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .apply("DATE(create_time) = CURDATE()"));

        long onlineDrivers = driverMapper.selectCount(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getStatus, 1));

        List<Order> todayCompletedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 4)
                .apply("DATE(create_time) = CURDATE()"));
        BigDecimal todayRevenue = todayCompletedOrders.stream()
                .map(o -> o.getFinalPrice() != null ? o.getFinalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long alertCount = riskAlertMapper.selectCount(new LambdaQueryWrapper<RiskAlert>()
                .eq(RiskAlert::getHandled, 0));

        Map<String, Object> stats = Map.of(
                "totalUsers", totalUsers,
                "todayOrders", todayOrders,
                "onlineDrivers", onlineDrivers,
                "todayRevenue", todayRevenue,
                "alertCount", alertCount
        );
        return Result.ok(stats);
    }
}
