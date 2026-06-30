package com.jiangnan.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.vo.DailyOrderStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as orderCount, " +
            "COALESCE(SUM(CASE WHEN status = 4 THEN final_price ELSE 0 END), 0) as revenue " +
            "FROM t_order " +
            "WHERE create_time >= DATE_ADD(CURDATE(), INTERVAL -7 DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<DailyOrderStatVO> selectDailyOrderStats();
}
