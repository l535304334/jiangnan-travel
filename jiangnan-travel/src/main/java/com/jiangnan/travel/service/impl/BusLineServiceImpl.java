package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.entity.BusLine;
import com.jiangnan.travel.entity.BusSchedule;
import com.jiangnan.travel.mapper.BusLineMapper;
import com.jiangnan.travel.mapper.BusScheduleMapper;
import com.jiangnan.travel.service.BusLineService;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.vo.BusLineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusLineServiceImpl implements BusLineService {

    private final BusLineMapper busLineMapper;
    private final BusScheduleMapper busScheduleMapper;
    private final NotificationService notificationService;

    @Override
    public List<BusLineVO> listLines(String startCity, String endCity) {
        LambdaQueryWrapper<BusLine> wrapper = new LambdaQueryWrapper<BusLine>()
                .eq(BusLine::getStatus, 1)
                .orderByAsc(BusLine::getId);

        if (startCity != null && !startCity.isEmpty()) {
            wrapper.eq(BusLine::getStartCity, startCity);
        }
        if (endCity != null && !endCity.isEmpty()) {
            wrapper.eq(BusLine::getEndCity, endCity);
        }

        return busLineMapper.selectList(wrapper).stream()
                .map(this::toVOWithoutSchedules)
                .collect(Collectors.toList());
    }

    @Override
    public BusLineVO getLineDetail(Long lineId) {
        BusLine line = busLineMapper.selectById(lineId);
        if (line == null || line.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班线不存在");
        }

        List<BusSchedule> schedules = busScheduleMapper.selectList(
                new LambdaQueryWrapper<BusSchedule>()
                        .eq(BusSchedule::getLineId, lineId)
                        .eq(BusSchedule::getStatus, 1)
                        .orderByAsc(BusSchedule::getDepartTime));

        List<BusLineVO.ScheduleVO> scheduleVOs = schedules.stream()
                .map(s -> BusLineVO.ScheduleVO.builder()
                        .id(s.getId())
                        .departTime(s.getDepartTime().toString())
                        .arriveTime(s.getArriveTime().toString())
                        .ticketCount(s.getTicketCount())
                        .remaining(s.getRemaining())
                        .status(s.getStatus())
                        .build())
                .collect(Collectors.toList());

        BusLineVO vo = toVOWithoutSchedules(line);
        vo.setSchedules(scheduleVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusLineVO.ScheduleVO purchaseTicket(Long scheduleId, Long userId) {
        BusSchedule schedule = busScheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getStatus() == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班次不存在");
        }
        if (schedule.getRemaining() <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该班次已售罄");
        }

        schedule.setRemaining(schedule.getRemaining() - 1);
        busScheduleMapper.updateById(schedule);

        BusLine line = busLineMapper.selectById(schedule.getLineId());
        notificationService.create(userId, "TICKET_PURCHASED",
                "购票成功",
                "已购买 " + line.getLineName() + " " + schedule.getDepartTime() + " 班次",
                scheduleId);

        log.info("用户[{}]购票成功，班次[{}]，余票[{}]", userId, scheduleId, schedule.getRemaining());
        return BusLineVO.ScheduleVO.builder()
                .id(schedule.getId())
                .departTime(schedule.getDepartTime().toString())
                .arriveTime(schedule.getArriveTime().toString())
                .ticketCount(schedule.getTicketCount())
                .remaining(schedule.getRemaining())
                .status(schedule.getStatus())
                .build();
    }

    @Override
    public Page<BusLine> adminList(Integer page, Integer size) {
        Page<BusLine> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 10);
        return busLineMapper.selectPage(pageParam,
                new LambdaQueryWrapper<BusLine>()
                        .orderByDesc(BusLine::getCreateTime));
    }

    @Override
    public void adminSave(BusLine line) {
        line.setId(null);
        busLineMapper.insert(line);
    }

    @Override
    public void adminUpdate(BusLine line) {
        busLineMapper.updateById(line);
    }

    @Override
    public void adminDelete(Long id) {
        busLineMapper.deleteById(id);
    }

    private BusLineVO toVOWithoutSchedules(BusLine line) {
        String typeName = switch (line.getBusType() != null ? line.getBusType() : "regular") {
            case "express" -> "快线";
            case "商务" -> "商务车";
            default -> "豪华大巴";
        };
        return BusLineVO.builder()
                .id(line.getId())
                .lineName(line.getLineName())
                .startCity(line.getStartCity())
                .endCity(line.getEndCity())
                .busType(line.getBusType())
                .busTypeName(typeName)
                .duration(line.getDuration())
                .price(line.getPrice())
                .distance(line.getDistance())
                .status(line.getStatus())
                .build();
    }
}
