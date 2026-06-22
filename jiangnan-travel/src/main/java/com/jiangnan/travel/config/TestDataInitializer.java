package com.jiangnan.travel.config;

import com.jiangnan.travel.entity.*;
import com.jiangnan.travel.mapper.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TestDataInitializer {

    private final UserMapper userMapper;
    private final DriverMapper driverMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("test:data:initialized"))) {
            return;
        }
        String pwd = passwordEncoder.encode("123456");

        createUser("13920000001", pwd, "南昌旅客");
        createUser("13920000002", pwd, "赣州游客");
        createUser("13920000003", pwd, "宁都通勤族");

        createDriver("13810000001", pwd, "南昌张师傅", "赣A12345", 1L, 28.6842, 115.8759, "4.80");
        createDriver("13810000002", pwd, "赣州李师傅", "赣B67890", 2L, 25.8456, 114.9356, "4.90");
        createDriver("13810000003", pwd, "宁都王师傅", "赣C11111", 3L, 26.4825, 116.0209, "4.70");

        redisTemplate.opsForValue().set("test:data:initialized", "true");
        System.out.println("=== 测试账号初始化完成 ===");
    }

    private void createUser(String phone, String pwd, String nickname) {
        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone);
        if (userMapper.selectCount(q) > 0) return;
        User u = new User(); u.setPhone(phone); u.setPassword(pwd);
        u.setNickname(nickname); u.setStatus(1);
        userMapper.insert(u);
    }

    private void createDriver(String phone, String pwd, String name,
                               String plate, Long ct, double lat, double lng, String rating) {
        createUser(phone, pwd, name);
        User u = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));

        var q = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Driver>()
                .eq(Driver::getUserId, u.getId());
        if (driverMapper.selectCount(q) > 0) return;

        Driver d = new Driver(); d.setUserId(u.getId()); d.setRealName(name);
        d.setCarPlate(plate); d.setCarTypeId(ct); d.setStatus(1);
        d.setVerifyStatus(1); d.setLat(BigDecimal.valueOf(lat));
        d.setLng(BigDecimal.valueOf(lng)); d.setAvgRating(new BigDecimal(rating));
        driverMapper.insert(d);
    }
}
