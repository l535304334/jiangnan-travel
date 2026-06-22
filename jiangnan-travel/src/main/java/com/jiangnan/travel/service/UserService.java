package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.LoginRequest;
import com.jiangnan.travel.dto.PasswordLoginRequest;
import com.jiangnan.travel.dto.RegisterRequest;
import com.jiangnan.travel.entity.User;
import com.jiangnan.travel.vo.LoginVO;

public interface UserService {

    LoginVO login(LoginRequest request);

    LoginVO passwordLogin(PasswordLoginRequest request);

    LoginVO register(RegisterRequest request);

    void sendCode(String phone);

    User getProfile(Long userId);

    User updateProfile(Long userId, String nickname, String avatar);
}
