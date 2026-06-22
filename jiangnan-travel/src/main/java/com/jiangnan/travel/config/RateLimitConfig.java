package com.jiangnan.travel.config;

import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig implements WebMvcConfigurer {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(redisTemplate))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/doc.html", "/webjars/**");
    }

    static class RateLimitInterceptor implements HandlerInterceptor {

        private final StringRedisTemplate redisTemplate;
        private static final String RATE_LIMIT_PREFIX = "rate:limit:";
        private static final int MAX_REQUESTS = 60; // 每分钟60次
        private static final int WINDOW_SECONDS = 60;

        RateLimitInterceptor(StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) {
            String ip = getClientIp(request);
            String key = RATE_LIMIT_PREFIX + ip;

            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count != null && count > MAX_REQUESTS) {
                throw new BusinessException(ErrorCode.RATE_LIMIT);
            }
            return true;
        }
    }

    static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
