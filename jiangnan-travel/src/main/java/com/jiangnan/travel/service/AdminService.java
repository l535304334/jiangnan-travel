package com.jiangnan.travel.service;

import com.jiangnan.travel.vo.LoginVO;

public interface AdminService {
    LoginVO login(String username, String password);
}
