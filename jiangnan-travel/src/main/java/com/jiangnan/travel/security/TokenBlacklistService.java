package com.jiangnan.travel.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWT 黑名单 — 基于 Redis。
 * 登出/改密后，将 token 加入黑名单（TTL = token 剩余有效期），
 * 在 Filter 和 WebSocket onOpen 中校验时先查黑名单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:token:";
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    /** 将 token 加入黑名单，TTL 等于其剩余有效期 */
    public void blacklist(String token) {
        try {
            long remaining = jwtUtil.getRemainingMs(token);
            if (remaining <= 0) return; // already expired
            String key = PREFIX + hashToken(token);
            redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(remaining));
            log.debug("Token 已加入黑名单, 剩余 {}ms", remaining);
        } catch (Exception e) {
            log.warn("Token 黑名单操作失败", e);
        }
    }

    /** 检查 token 是否在黑名单中 */
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + hashToken(token)));
        } catch (Exception e) {
            log.warn("Token 黑名单查询失败, 默认放行", e);
            return false; // Redis 不可用时默认放行，避免全部用户被拒
        }
    }

    /** 取 token 前 32 字符做 key，避免 key 过长 */
    private static String hashToken(String token) {
        return token.length() > 32 ? token.substring(0, 32) : token;
    }
}
