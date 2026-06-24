package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.UserAddress;
import com.jiangnan.travel.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
@Tag(name = "用户地址", description = "用户收藏地址管理")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @Data
    static class AddReq { String tag; String address; BigDecimal lat; BigDecimal lng; }

    @GetMapping
    @Operation(summary = "地址列表", description = "获取用户收藏的地址列表")
    public Result<List<UserAddress>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userAddressService.listByUser(userId));
    }

    @PostMapping
    @Operation(summary = "新增地址", description = "新增收藏地址")
    public Result<UserAddress> add(@RequestBody AddReq req, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserAddress addr = userAddressService.add(userId,
                req.tag != null ? req.tag : "自定义",
                req.address != null ? req.address : "",
                req.lat != null ? req.lat : BigDecimal.ZERO,
                req.lng != null ? req.lng : BigDecimal.ZERO);
        return Result.ok(addr);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除指定收藏地址")
    public Result<?> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userAddressService.delete(userId, id);
        return Result.ok();
    }
}
