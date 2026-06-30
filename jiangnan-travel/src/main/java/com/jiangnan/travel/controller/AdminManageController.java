package com.jiangnan.travel.controller;

import com.jiangnan.travel.annotation.LogOperation;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.*;
import com.jiangnan.travel.entity.BusLine;
import com.jiangnan.travel.entity.Campaign;
import com.jiangnan.travel.entity.VipLevel;
import com.jiangnan.travel.mapper.VipLevelMapper;
import com.jiangnan.travel.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台", description = "管理后台用户、司机、订单、风控管理")
public class AdminManageController {

    private final UserService userService;
    private final DriverService driverService;
    private final OrderService orderService;
    private final RiskAlertService riskAlertService;
    private final CarTypeService carTypeService;
    private final CampaignService campaignService;
    private final BusLineService busLineService;
    private final VipLevelMapper vipLevelMapper;

    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                Authentication authentication) {
        return Result.ok(userService.listUsers(page, size));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "修改用户状态", description = "修改指定用户账号状态")
    @LogOperation("修改用户状态")
    public Result<?> updateUserStatus(@PathVariable Long id,
                                       @Valid @RequestBody UpdateUserStatusRequest request,
                                       Authentication authentication) {
        Integer status = request.getStatus();
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            return Result.fail("无效的状态值");
        }
        userService.updateUserStatus(id, status);
        return Result.ok("状态更新成功");
    }

    @GetMapping("/drivers")
    @Operation(summary = "司机列表", description = "分页查询司机列表")
    public Result<?> listDrivers(@RequestParam(required = false) Integer verifyStatus,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size,
                                  Authentication authentication) {
        return Result.ok(driverService.listDrivers(verifyStatus, page, size));
    }

    @PutMapping("/drivers/{id}/verify")
    @Operation(summary = "审核司机", description = "审核司机认证信息")
    @LogOperation("审核司机")
    public Result<?> verifyDriver(@PathVariable Long id,
                                   @Valid @RequestBody VerifyDriverRequest request,
                                   Authentication authentication) {
        Integer verifyStatus = request.getStatus();
        if (verifyStatus == null || (verifyStatus != 1 && verifyStatus != 2)) {
            return Result.fail("无效的审核状态值");
        }
        driverService.verifyDriver(id, verifyStatus);
        return Result.ok("审核操作成功");
    }

    @GetMapping("/orders")
    @Operation(summary = "订单列表", description = "分页查询订单列表")
    public Result<?> listOrders(@RequestParam(required = false) Integer status,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 Authentication authentication) {
        return Result.ok(orderService.listOrders(status, page, size));
    }

    @GetMapping("/alerts")
    @Operation(summary = "告警列表", description = "分页查询风控告警")
    public Result<?> listAlerts(@RequestParam(required = false) Integer handled,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 Authentication authentication) {
        return Result.ok(riskAlertService.listAlerts(handled, page, size));
    }

    @PutMapping("/alerts/{id}/handle")
    @Operation(summary = "处理风控告警", description = "处理指定风控告警记录")
    @LogOperation("处理风控告警")
    public Result<?> handleAlert(@PathVariable Long id,
                                  @Valid @RequestBody HandleAlertRequest request,
                                  Authentication authentication) {
        riskAlertService.handleAlert(id, request.getHandleRemark());
        return Result.ok("预警处理成功");
    }

    @GetMapping("/car-types")
    @Operation(summary = "车型列表", description = "查询车型定价列表")
    public Result<?> listCarTypes(Authentication authentication) {
        return Result.ok(carTypeService.listAll());
    }

    @PutMapping("/car-types/{id}")
    @Operation(summary = "修改车型定价", description = "更新车型定价信息")
    @LogOperation("修改车型定价")
    public Result<?> updateCarType(@PathVariable Long id,
                                    @Valid @RequestBody UpdateCarTypeRequest request,
                                    Authentication authentication) {
        carTypeService.update(id, request);
        return Result.ok("车型更新成功");
    }

    @GetMapping("/dashboard")
    @Operation(summary = "数据大屏", description = "获取管理后台统计数据")
    @Cacheable(value = "dashboard", key = "'stats'")
    public Result<?> dashboard(Authentication authentication) {
        long totalUsers = userService.countUsers();
        long todayOrders = orderService.countTodayOrders();
        long onlineDrivers = driverService.countOnlineDrivers();
        BigDecimal todayRevenue = orderService.getTodayRevenue();
        long alertCount = riskAlertService.countUnhandledAlerts();

        Map<String, Object> stats = Map.of(
                "totalUsers", totalUsers,
                "todayOrders", todayOrders,
                "onlineDrivers", onlineDrivers,
                "todayRevenue", todayRevenue,
                "alertCount", alertCount
        );
        return Result.ok(stats);
    }

    @GetMapping("/dashboard/chart")
    @Operation(summary = "图表数据", description = "获取近7天订单趋势和收入趋势")
    @Cacheable(value = "dashboard", key = "'chart'")
    public Result<?> chartData(Authentication authentication) {
        return Result.ok(orderService.getLast7DaysStats());
    }

    /* ===== 活动管理 ===== */

    @GetMapping("/campaigns")
    @Operation(summary = "活动列表", description = "管理端活动分页列表")
    public Result<?> listCampaigns(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(campaignService.listAdmin(keyword, page, size));
    }

    @PostMapping("/campaigns")
    @Operation(summary = "创建活动", description = "创建新活动并关联优惠券")
    public Result<?> createCampaign(@Valid @RequestBody CreateCampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setBannerUrl(request.getBannerUrl());
        campaign.setStartTime(request.getStartTime());
        campaign.setEndTime(request.getEndTime());
        campaign.setType(request.getType() != null ? request.getType() : 0);
        campaign.setStatus(0);
        campaignService.create(campaign, request.getCouponIds());
        return Result.ok("创建成功");
    }

    @PutMapping("/campaigns/{id}")
    @Operation(summary = "更新活动", description = "更新活动信息和关联优惠券")
    public Result<?> updateCampaign(@PathVariable Long id,
                                     @Valid @RequestBody CreateCampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setId(id);
        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setBannerUrl(request.getBannerUrl());
        campaign.setStartTime(request.getStartTime());
        campaign.setEndTime(request.getEndTime());
        campaign.setType(request.getType() != null ? request.getType() : 0);
        campaignService.update(campaign, request.getCouponIds());
        return Result.ok("更新成功");
    }

    @DeleteMapping("/campaigns/{id}")
    @Operation(summary = "删除活动", description = "删除活动")
    public Result<?> deleteCampaign(@PathVariable Long id) {
        campaignService.delete(id);
        return Result.ok("删除成功");
    }

    /* ===== VIP等级管理 ===== */
    @GetMapping("/vip-levels")
    @Operation(summary = "VIP等级列表", description = "管理端查看所有VIP等级")
    public Result<?> listVipLevels() {
        return Result.ok(vipLevelMapper.selectList(null));
    }

    @PostMapping("/vip-levels/create")
    @Operation(summary = "创建VIP等级", description = "创建新的VIP等级")
    public Result<?> createVipLevel(@Valid @RequestBody VipLevel level) {
        level.setId(null);
        vipLevelMapper.insert(level);
        return Result.ok("创建成功");
    }

    @PutMapping("/vip-levels/{id}")
    @Operation(summary = "更新VIP等级", description = "更新VIP等级信息")
    public Result<?> updateVipLevel(@PathVariable Long id, @Valid @RequestBody VipLevel level) {
        level.setId(id);
        vipLevelMapper.updateById(level);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/vip-levels/{id}")
    @Operation(summary = "删除VIP等级", description = "删除VIP等级")
    public Result<?> deleteVipLevel(@PathVariable Long id) {
        vipLevelMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    /* ===== 班线管理 ===== */
    @GetMapping("/bus-lines")
    @Operation(summary = "班线列表", description = "管理端查看所有班线")
    public Result<?> listBusLines(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.ok(busLineService.adminList(page, size));
    }

    @PostMapping("/bus-lines/create")
    @Operation(summary = "创建班线", description = "创建新的城际班线")
    public Result<?> createBusLine(@Valid @RequestBody BusLine line) {
        busLineService.adminSave(line);
        return Result.ok("创建成功");
    }

    @PutMapping("/bus-lines/{id}")
    @Operation(summary = "更新班线", description = "更新班线信息")
    public Result<?> updateBusLine(@PathVariable Long id, @Valid @RequestBody BusLine line) {
        line.setId(id);
        busLineService.adminUpdate(line);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/bus-lines/{id}")
    @Operation(summary = "删除班线", description = "删除班线")
    public Result<?> deleteBusLine(@PathVariable Long id) {
        busLineService.adminDelete(id);
        return Result.ok("删除成功");
    }
}
