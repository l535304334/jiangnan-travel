package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.Driver;

import java.util.List;

public interface DispatchService {

    Driver findBestDriver(Long orderId);

    List<Driver> findNearbyDrivers(Long orderId, int limit);
}
