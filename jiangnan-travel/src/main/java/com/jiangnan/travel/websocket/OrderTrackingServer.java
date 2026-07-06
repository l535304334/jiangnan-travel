package com.jiangnan.travel.websocket;

import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.security.TokenBlacklistService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/order/{orderId}", configurator = JwtCookieConfigurator.class)
public class OrderTrackingServer {

    private static JwtUtil jwtUtil;
    private static TokenBlacklistService blacklistService;
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Session>> orderSessions = new ConcurrentHashMap<>();
    /** L-05 修复：全局连接数限流，防止 WebSocket DoS */
    private static final AtomicInteger totalConnections = new AtomicInteger(0);
    private static final int MAX_TOTAL_CONNECTIONS = 1000;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        OrderTrackingServer.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setBlacklistService(TokenBlacklistService blacklistService) {
        OrderTrackingServer.blacklistService = blacklistService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("orderId") Long orderId) {
        String token = JwtCookieConfigurator.getToken(session);
        if (token == null || !jwtUtil.validateToken(token) ||
                (blacklistService != null && blacklistService.isBlacklisted(token))) {
            try { session.close(); } catch (Exception ignored) {}
            return;
        }
        if (totalConnections.incrementAndGet() > MAX_TOTAL_CONNECTIONS) {
            totalConnections.decrementAndGet();
            log.warn("WebSocket 全局连接数超过上限 {}，拒绝新连接", MAX_TOTAL_CONNECTIONS);
            try { session.close(); } catch (Exception ignored) {}
            return;
        }
        orderSessions.computeIfAbsent(orderId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        log.info("订单[{}] 用户已订阅", orderId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("orderId") Long orderId) {
        ConcurrentHashMap<String, Session> map = orderSessions.get(orderId);
        boolean wasInMap = false;
        if (map != null) {
            wasInMap = map.remove(session.getId()) != null;
            if (map.isEmpty()) orderSessions.remove(orderId);
        }
        if (wasInMap) {
            totalConnections.decrementAndGet();
        }
    }

    @OnError
    public void onError(Session session, @PathParam("orderId") Long orderId, Throwable e) {
        onClose(session, orderId);
    }

    public static void pushOrderUpdate(Long orderId, String message) {
        ConcurrentHashMap<String, Session> map = orderSessions.get(orderId);
        if (map == null) return;
        for (Session s : map.values()) {
            if (s.isOpen()) { try { s.getBasicRemote().sendText(message); } catch (IOException ignored) {} }
        }
    }
}
