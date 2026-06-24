package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_city_landmark")
public class CityLandmark extends BaseEntity {

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
}
