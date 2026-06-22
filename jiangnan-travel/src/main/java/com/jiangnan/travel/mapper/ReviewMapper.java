package com.jiangnan.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangnan.travel.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ReviewMapper extends BaseMapper<Review> {
}
