package com.jiangnan.travel.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 1000-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PHONE_EXISTS(1002, "手机号已被注册"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_STATUS_DISABLED(1004, "账号已被禁用"),
    SMS_CODE_ERROR(1005, "验证码错误"),
    SMS_CODE_EXPIRED(1006, "验证码已过期"),

    // 订单模块 2000-2999
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态不正确"),
    ORDER_CANNOT_CANCEL(2003, "订单无法取消"),
    ORDER_DRIVER_BUSY(2004, "司机忙碌中"),
    ORDER_CAR_TYPE_INVALID(2005, "车型不可用"),

    // 司机模块 3000-3999
    DRIVER_NOT_FOUND(3001, "司机不存在"),
    DRIVER_NOT_VERIFIED(3002, "司机未通过审核"),
    DRIVER_OFFLINE(3003, "司机已下线"),

    // 风控模块 4000-4999
    RISK_ORDER_FREQUENCY(4001, "下单频率异常"),
    RISK_CANCEL_RATE(4002, "取消率过高，暂时无法下单"),
    RISK_ACCOUNT_FROZEN(4003, "账号已被风控冻结"),

    // AI模块 5000-5999
    AI_SERVICE_ERROR(5001, "AI服务暂时不可用"),
    AI_QUOTA_EXCEEDED(5002, "AI调用次数超限"),

    // 支付模块 6000-6999
    PAY_FAILED(6001, "支付失败"),
    PAY_AMOUNT_MISMATCH(6002, "支付金额不匹配"),

    // 系统模块 9000-9999
    SYSTEM_BUSY(9001, "系统繁忙，请稍后重试"),
    RATE_LIMIT(9002, "请求过于频繁，请稍后重试");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
