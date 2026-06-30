package com.jiangnan.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.entity.BusLine;
import com.jiangnan.travel.vo.BusLineVO;

import java.util.List;

public interface BusLineService {

    List<BusLineVO> listLines(String startCity, String endCity);

    BusLineVO getLineDetail(Long lineId);

    BusLineVO.ScheduleVO purchaseTicket(Long scheduleId, Long userId);

    /* ===== 管理后台 ===== */
    Page<BusLine> adminList(Integer page, Integer size);

    void adminSave(BusLine line);

    void adminUpdate(BusLine line);

    void adminDelete(Long id);
}
