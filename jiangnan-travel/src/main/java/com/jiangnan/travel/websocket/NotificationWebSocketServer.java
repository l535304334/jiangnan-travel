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
@ServerEndpoint("/ws/notification/{userId}")
/**
 * WebSocket 端点由容器管理实例（非 Spring），
 * 因此使用静态 setter 注入（@Autowired 异常豁免）。
 */
public class NotificationWebSocketServer {

    private static JwtUtil jwtUtil;
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Session>> userSessions = new ConcurrentHashMap<>();

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        NotificationWebSocketServer.jwtUtil = jwtUtil;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        String token = getParam(session, "token");
        if (token == null || !jwtUtil.validateToken(token)) {
            try { session.close(); } catch (Exception ignored) {}
            return;
        }
        // 验证 token 中的 userId 与路径一致
        Long tokenUserId = jwtUtil.getUserId(token);
        if (!userId.equals(tokenUserId)) {
            try { session.close(); } catch (Exception ignored) {}
            return;
        }
        userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        log.info("用户[{}] 已连接通知WebSocket", userId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        ConcurrentHashMap<String, Session> map = userSessions.get(userId);
        if (map != null) {
            map.remove(session.getId());
            if (map.isEmpty()) userSessions.remove(userId);
        }
    }

    @OnError
    public void onError(Session session, @PathParam("userId") Long userId, Throwable e) {
        onClose(session, userId);
    }

    public static void pushToUser(Long userId, String message) {
        ConcurrentHashMap<String, Session> map = userSessions.get(userId);
        if (map == null) return;
        for (Session s : map.values()) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(message);
                } catch (IOException ignored) {}
            }
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
