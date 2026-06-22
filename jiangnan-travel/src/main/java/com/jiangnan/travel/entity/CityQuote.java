package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_city_quote")
public class CityQuote {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String city;
    private String content;
    private String author;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
