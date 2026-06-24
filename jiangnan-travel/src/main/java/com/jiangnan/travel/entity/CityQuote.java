package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_city_quote")
public class CityQuote extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String city;
    private String content;
    private String author;
    private Integer sort;
    private Integer status;
}
