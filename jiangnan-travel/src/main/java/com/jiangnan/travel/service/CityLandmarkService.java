package com.jiangnan.travel.service;

import com.jiangnan.travel.entity.CityLandmark;

import java.util.List;

public interface CityLandmarkService {

    List<CityLandmark> listAll();

    List<CityLandmark> search(String keyword);
}
