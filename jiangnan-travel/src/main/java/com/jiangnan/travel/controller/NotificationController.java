package com.jiangnan.travel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.entity.Notification;
import com.jiangnan.travel.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "消息通知", description = "消息通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    @Operation(summary = "获取通知列表")
    public Result<IPage<Notification>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(notificationService.listByUser(userId, pageNum, pageSize));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Long> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条通知为已读")
    public Result<Void> markRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markRead(id, userId);
        return Result.ok();
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记全部通知为已读")
    public Result<Void> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return Result.ok();
    }
}
