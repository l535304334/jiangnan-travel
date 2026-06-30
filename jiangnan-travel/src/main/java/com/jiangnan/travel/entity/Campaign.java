package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_campaign")
public class Campaign extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String bannerUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer type;
    private Integer status;
}
