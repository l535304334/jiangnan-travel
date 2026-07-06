package com.jiangnan.travel.websocket;

import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.security.TokenBlacklistService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/driver-location", configurator = JwtCookieConfigurator.class)
public class DriverLocationServer {

    private static JwtUtil jwtUtil;
    private static TokenBlacklistService blacklistService;
    private static final ConcurrentHashMap<Long, Session> driverSessions = new ConcurrentHashMap<>();
    /** L-05 修复：全局连接数限流，防止 WebSocket DoS */
    private static final AtomicInteger totalConnections = new AtomicInteger(0);
    private static final int MAX_TOTAL_CONNECTIONS = 500;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        DriverLocationServer.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setBlacklistService(TokenBlacklistService blacklistService) {
        DriverLocationServer.blacklistService = blacklistService;
    }

    @OnOpen
    public void onOpen(Session session) {
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
        Long driverId = jwtUtil.getUserId(token);
        driverSessions.put(driverId, session);
        log.info("司机[{}] WS 已连接", driverId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // message: "ping" -> "pong" 或 "orderId,lat,lng" -> 转发给订单订阅者
        Long driverId = getDriverId(session);
        if (driverId == null) return;
        String trimmed = message.trim();
        if ("ping".equals(trimmed)) {
            try { session.getBasicRemote().sendText("pong"); } catch (IOException ignored) {}
        } else if (trimmed.contains(",")) {
            String[] parts = trimmed.split(",");
            if (parts.length >= 3) {
                try {
                    Long orderId = Long.parseLong(parts[0].trim());
                    BigDecimal lat = new BigDecimal(parts[1].trim());
                    BigDecimal lng = new BigDecimal(parts[2].trim());
                    String json = String.format(
                        "{\"type\":\"DRIVER_LOCATION\",\"orderId\":%d,\"lat\":%s,\"lng\":%s}",
                        orderId, lat.toPlainString(), lng.toPlainString()
                    );
                    OrderTrackingServer.pushOrderUpdate(orderId, json);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long driverId = getDriverId(session);
        if (driverId != null) {
            Session removed = driverSessions.remove(driverId);
            if (removed != null) {
                totalConnections.decrementAndGet();
            }
            log.info("司机[{}] WS 已断开", driverId);
        }
    }

    @OnError
    public void onError(Session session, Throwable e) {
        onClose(session);
    }

    public static void sendToDriver(Long driverId, String msg) {
        Session s = driverSessions.get(driverId);
        if (s != null && s.isOpen()) {
            try { s.getBasicRemote().sendText(msg); } catch (IOException ignored) {}
        }
    }

    private Long getDriverId(Session session) {
        for (var entry : driverSessions.entrySet()) {
            if (entry.getValue().equals(session)) return entry.getKey();
        }
        return null;
    }
}
