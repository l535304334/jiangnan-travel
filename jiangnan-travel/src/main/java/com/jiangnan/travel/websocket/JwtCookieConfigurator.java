package com.jiangnan.travel.websocket;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截器 — 从 Cookie 提取 JWT 存入 session userProperties。
 * 替代 URL query param 方式，避免 token 出现在代理/服务器日志中。
 */
public class JwtCookieConfigurator extends ServerEndpointConfig.Configurator {

    private static final String TOKEN_PROPERTY = "jwtToken";

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> headers = request.getHeaders();
        List<String> cookies = headers.get("cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                for (String pair : cookie.split(";\\s*")) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2 && "token".equals(kv[0].trim())) {
                        sec.getUserProperties().put(TOKEN_PROPERTY, kv[1].trim());
                        return;
                    }
                }
            }
        }
    }

    /** 从 session 中取出握手时存入的 JWT */
    public static String getToken(jakarta.websocket.Session session) {
        return (String) session.getUserProperties().get(TOKEN_PROPERTY);
    }
}
