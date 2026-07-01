package com.jiangnan.travel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangnan.travel.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 已禁用 — 纯 REST API + JWT Bearer 无状态架构，无需 CSRF。
                // 若未来改用 httpOnly Cookie 存储 JWT，需重新启用 CSRF 保护。
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // ============ 公开接口 ============
                        // 用户登录/注册/验证码
                        .requestMatchers("/api/user/register", "/api/user/login",
                                "/api/user/login-password", "/api/user/send-code").permitAll()
                        // 司机登录/注册
                        .requestMatchers("/api/driver/login", "/api/driver/register").permitAll()
                        // 管理员登录
                        .requestMatchers("/api/admin/login").permitAll()
                        // 公共接口 + 文旅 + WebSocket
                        .requestMatchers("/api/common/**",
                                "/api/landmark", "/api/landmark/**",
                                "/api/city-quote", "/api/city-quote/**",
                                "/ws/**").permitAll()
                        // ARCHITECTURE.md 公开的优惠券/活动/VIP/订单/班线/司机接口
                        .requestMatchers("/api/coupon/list",
                                "/api/campaign/list", "/api/campaign/{id}", "/api/campaign/available-coupons",
                                "/api/vip/levels", "/api/vip/benefits",
                                "/api/order/estimate",
                                "/api/bus-line/list",
                                "/api/driver/order/nearby").permitAll()
                        // 班线详情公开，购票需认证；注意顺序：purchase 必须在 {id} 之前
                        .requestMatchers("/api/bus-line/purchase").authenticated()
                        .requestMatchers("/api/bus-line/{id}").permitAll()
                        // Swagger/Knife4j
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/doc.html", "/webjars/**").permitAll()
                        // Actuator / Prometheus 监控
                        .requestMatchers("/actuator/health", "/actuator/info",
                                "/actuator/prometheus", "/actuator/metrics").permitAll()

                        // ============ 角色路由 ============
                        // 管理员端 — 仅 ADMIN 角色
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 司机端 — 仅 DRIVER 角色（除 login/register 已在上面放行）
                        .requestMatchers("/api/driver/**").hasRole("DRIVER")
                        // 用户端 — 仅 USER 角色（除 login/register/send-code 已在上面放行）
                        .requestMatchers("/api/user/**").hasRole("USER")
                        // 订单操作需要授权（用户或司机均可访问自己的订单）
                        .requestMatchers("/api/order/**").authenticated()
                        // 安全相关
                        .requestMatchers("/api/safety/**").authenticated()
                        // 优惠券
                        .requestMatchers("/api/coupon/**").authenticated()
                        // VIP
                        .requestMatchers("/api/vip/**").authenticated()
                        // 支付（回调公开，其余需认证）
                        .requestMatchers("/api/payment/callback").permitAll()
                        .requestMatchers("/api/payment/**").authenticated()
                        // 发票
                        .requestMatchers("/api/invoice/**").authenticated()
                        // 用户地址
                        .requestMatchers("/api/address/**").authenticated()

                        // 其他需要认证
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            new ObjectMapper().writeValue(response.getWriter(), Result.fail(401, "未登录或登录已过期"));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            new ObjectMapper().writeValue(response.getWriter(), Result.fail(403, "无权限访问"));
        };
    }
}
