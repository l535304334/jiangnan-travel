package com.jiangnan.travel.websocket;

import com.jiangnan.travel.security.JwtUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/order/{orderId}")
public class OrderTrackingServer {

    private static JwtUtil jwtUtil;
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Session>> orderSessions = new ConcurrentHashMap<>();

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        OrderTrackingServer.jwtUtil = jwtUtil;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("orderId") Long orderId) {
        String token = getParam(session, "token");
        if (token == null || !jwtUtil.validateToken(token)) {
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
        if (map != null) { map.remove(session.getId()); if (map.isEmpty()) orderSessions.remove(orderId); }
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
