package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.UserAddress;
import java.util.List;

public interface UserAddressService {

    List<UserAddress> listByUser(Long userId);

    UserAddress add(Long userId, String tag, String address, java.math.BigDecimal lat, java.math.BigDecimal lng);

    void delete(Long userId, Long addressId);
}
