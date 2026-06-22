package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.UserAddress;
import com.jiangnan.travel.service.UserAddressService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @Data
    static class AddReq { String tag; String address; BigDecimal lat; BigDecimal lng; }

    @GetMapping
    public Result<List<UserAddress>> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userAddressService.listByUser(userId));
    }

    @PostMapping
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
    public Result<?> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userAddressService.delete(userId, id);
        return Result.ok();
    }
}
