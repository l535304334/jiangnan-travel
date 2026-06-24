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
        // message: "ping" -> "pong" 或 "lat,lng" -> 广播给相关订单
        Long driverId = getDriverId(session);
        if (driverId == null) return;
        if ("ping".equals(message.trim())) {
            try { session.getBasicRemote().sendText("pong"); } catch (IOException ignored) {}
        } else if (message.contains(",")) {
            String[] parts = message.split(",");
            if (parts.length >= 2) {
                try {
                    BigDecimal lat = new BigDecimal(parts[0].trim());
                    BigDecimal lng = new BigDecimal(parts[1].trim());
                    log.debug("司机[{}]位置更新: {},{}", driverId, lat, lng);
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
