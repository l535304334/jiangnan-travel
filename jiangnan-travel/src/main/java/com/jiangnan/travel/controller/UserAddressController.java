package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "用户地址", description = "用户收藏地址管理")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @Data
    static class AddReq {
        @Size(max = 20) String tag;
        @NotBlank @Size(max = 200) String address;
        BigDecimal lat;
        BigDecimal lng;
    }

    @GetMapping
    @Operation(summary = "地址列表", description = "获取用户收藏的地址列表")
    public Result<?> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userAddressService.listByUser(userId));
    }

    @PostMapping
    @Operation(summary = "新增地址", description = "新增收藏地址")
    public Result<?> add(@Valid @RequestBody AddReq req, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userAddressService.add(userId,
                req.tag != null ? req.tag : "自定义",
                req.address,
                req.lat != null ? req.lat : BigDecimal.ZERO,
                req.lng != null ? req.lng : BigDecimal.ZERO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址", description = "删除指定收藏地址")
    public Result<?> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userAddressService.delete(userId, id);
        return Result.ok();
    }
}
