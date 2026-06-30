package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.entity.Notification;
import com.jiangnan.travel.mapper.NotificationMapper;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.websocket.NotificationWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void create(Long userId, String type, String title, String content, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        // 通过 WebSocket 实时推送
        String message = String.format(
                "{\"id\":%d,\"type\":\"%s\",\"title\":\"%s\",\"content\":\"%s\",\"relatedId\":%d,\"createTime\":\"%s\"}",
                notification.getId(), type, title, content,
                relatedId != null ? relatedId : 0,
                notification.getCreateTime() != null ? notification.getCreateTime() : ""
        );
        NotificationWebSocketServer.pushToUser(userId, message);
        log.info("通知已发送 userId={} type={} title={}", userId, type, title);
    }

    @Override
    public IPage<Notification> listByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20),
                wrapper);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }

    @Override
    @Transactional
    public void markRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        Notification update = new Notification();
        update.setIsRead(1);
        notificationMapper.update(update,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
    }
}
