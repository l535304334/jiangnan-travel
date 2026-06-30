package com.jiangnan.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangnan.travel.entity.Campaign;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CampaignMapper extends BaseMapper<Campaign> {
}
