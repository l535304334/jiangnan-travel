package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.UserAddress;
import com.jiangnan.travel.mapper.UserAddressMapper;
import com.jiangnan.travel.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> listByUser(Long userId) {
        return userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getCreateTime));
    }

    @Override
    public UserAddress add(Long userId, String tag, String address, BigDecimal lat, BigDecimal lng) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setTag(tag);
        addr.setAddress(address);
        addr.setLat(lat != null ? lat : BigDecimal.ZERO);
        addr.setLng(lng != null ? lng : BigDecimal.ZERO);
        userAddressMapper.insert(addr);
        return addr;
    }

    @Override
    public void delete(Long userId, Long addressId) {
        UserAddress addr = userAddressMapper.selectById(addressId);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        userAddressMapper.deleteById(addressId);
    }
}
