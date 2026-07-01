package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.Admin;
import com.jiangnan.travel.mapper.AdminMapper;
import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.service.AdminService;
import com.jiangnan.travel.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String ADMIN_LOGIN_PREFIX = "login:admin:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 15;

    @Override
    public LoginVO login(String username, String password) {
        String attemptKey = ADMIN_LOGIN_PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(attemptKey);
        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            throw new BusinessException(ErrorCode.RATE_LIMIT);
        }

        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null || admin.getStatus() == 0) {
            incrementAttempts(attemptKey);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            incrementAttempts(attemptKey);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        redisTemplate.delete(attemptKey);

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), "ADMIN");
        return LoginVO.builder()
                .token(token).userId(admin.getId())
                .phone(admin.getUsername()).nickname(admin.getRealName()).build();
    }

    private void incrementAttempts(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOCKOUT_MINUTES, TimeUnit.MINUTES);
        }
    }
}
