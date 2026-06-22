package com.jiangnan.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangnan.travel.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface PaymentMapper extends BaseMapper<Payment> {
}
