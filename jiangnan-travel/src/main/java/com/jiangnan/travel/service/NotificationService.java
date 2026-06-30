package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jiangnan.travel.entity.Notification;

public interface NotificationService {

    void create(Long userId, String type, String title, String content, Long relatedId);

    IPage<Notification> listByUser(Long userId, Integer pageNum, Integer pageSize);

    long countUnread(Long userId);

    void markRead(Long id, Long userId);

    void markAllRead(Long userId);
}
