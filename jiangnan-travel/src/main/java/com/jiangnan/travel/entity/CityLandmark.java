package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_city_landmark")
public class CityLandmark {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String city;
    private String name;
    private BigDecimal lat;
    private BigDecimal lng;
    private String description;
    private String imageUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
