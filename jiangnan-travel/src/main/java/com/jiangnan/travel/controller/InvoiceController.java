package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.ApplyInvoiceRequest;
import com.jiangnan.travel.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "发票管理", description = "电子发票申请、查询、开具")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/apply")
    @Operation(summary = "申请发票", description = "申请订单发票")
    public Result<?> apply(@Valid @RequestBody ApplyInvoiceRequest request,
                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(invoiceService.apply(request, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "发票详情", description = "查询发票详情")
    public Result<?> detail(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(invoiceService.getById(id, userId));
    }

    @GetMapping("/list")
    @Operation(summary = "发票列表", description = "用户发票记录列表")
    public Result<?> list(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(invoiceService.listByUser(userId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消申请", description = "取消待开具的发票")
    public Result<?> cancel(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        invoiceService.cancel(id, userId);
        return Result.ok("已取消");
    }

    @PutMapping("/{id}/issue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "开具发票(管理员)", description = "管理员开具发票")
    public Result<?> issue(@PathVariable Long id) {
        return Result.ok(invoiceService.issue(id));
    }
}
