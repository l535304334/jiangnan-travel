package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.LoginRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.mapper.UserMapper;
import com.jiangnan.travel.security.JwtUtil;
import com.jiangnan.travel.service.UserService;
import com.jiangnan.travel.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final CouponServiceImpl couponService;

    private static final String SMS_PREFIX = "sms:code:";
    private static final long SMS_CODE_EXPIRE = 5; // 5分钟

    @Override
    public void sendCode(String phone) {
        // 检查是否频繁发送（60秒内只能发一次）
        String rateLimitKey = SMS_PREFIX + "rate:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            throw new BusinessException(9002, "验证码发送过于频繁，请60秒后再试");
        }
        // 生成6位验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        // 存入Redis，5分钟有效
        redisTemplate.opsForValue().set(SMS_PREFIX + phone, code, SMS_CODE_EXPIRE, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);
        // TODO: 接入真实短信服务，当前仅打印日志
        System.out.println("=== 验证码 [" + phone + "]: " + code + " ===");
    }

    @Override
    public LoginVO login(LoginRequest request) {
        // 验证码校验
        String cachedCode = redisTemplate.opsForValue().get(SMS_PREFIX + request.getPhone());
        if (cachedCode == null) {
            throw new BusinessException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!cachedCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        // 验证码用后即删
        redisTemplate.delete(SMS_PREFIX + request.getPhone());

        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_STATUS_DISABLED);
        }
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.RISK_ACCOUNT_FROZEN);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public LoginVO passwordLogin(com.jiangnan.travel.dto.PasswordLoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_STATUS_DISABLED);
        }
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.RISK_ACCOUNT_FROZEN);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public LoginVO register(RegisterRequest request) {
        // 验证码校验
        String cachedCode = redisTemplate.opsForValue().get(SMS_PREFIX + request.getPhone());
        if (cachedCode == null) {
            throw new BusinessException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!cachedCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete(SMS_PREFIX + request.getPhone());

        // 检查手机号是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_PHONE_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : "用户" + request.getPhone().substring(7));
        user.setStatus(1);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.insert(user);

        // 发放新用户优惠券
        couponService.grantNewUserCoupons(user.getId());

        // 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateProfile(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
}
