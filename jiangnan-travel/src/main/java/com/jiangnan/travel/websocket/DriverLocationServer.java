package com.jiangnan.travel.websocket;

import com.jiangnan.travel.security.JwtUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/driver-location")
/**
 * WebSocket 端点由容器管理实例（非 Spring），
 * 因此使用静态 setter 注入（@Autowired 异常豁免）。
 * 参考：PROJECT_RULES.md 3.4
 */
public class DriverLocationServer {

    private static JwtUtil jwtUtil;
    private static final ConcurrentHashMap<Long, Session> driverSessions = new ConcurrentHashMap<>();

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        DriverLocationServer.jwtUtil = jwtUtil;
    }

    @OnOpen
    public void onOpen(Session session) {
        String token = getParam(session, "token");
        if (token == null || !jwtUtil.validateToken(token)) {
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
            driverSessions.remove(driverId);
            log.info("司机[{}] WS 已断开", driverId);
        }
    }

    @OnError
    public void onError(Session session, Throwable e) {
        Long driverId = getDriverId(session);
        if (driverId != null) driverSessions.remove(driverId);
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

    private String getParam(Session session, String key) {
        String query = session.getRequestURI().getQuery();
        if (query == null) return null;
        for (String p : query.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }
}
