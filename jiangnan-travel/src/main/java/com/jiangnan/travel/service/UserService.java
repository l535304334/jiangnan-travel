package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    User updateProfile(Long userId, String nickname, String avatar, String phone);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    Page<User> listUsers(int page, int size);

    void updateUserStatus(Long id, Integer status);

    long countUsers();
}
