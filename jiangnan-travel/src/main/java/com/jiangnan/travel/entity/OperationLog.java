package com.jiangnan.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String role;
    private String username;
    private String operation;
    private String method;
    private String path;
    private String requestParams;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Integer durationMs;
    private LocalDateTime createTime;
}
