package com.jiangnan.travel.enums;

/**
 * 司机运行状态模型。
 * ponytail: enum-based, no runtime state machine needed.
 */
public enum DriverStatus {

    OFFLINE(0, "离线"),
    ONLINE_IDLE(1, "在线空闲"),
    BUSY(2, "接单中"),
    ASSIGNMENT_PENDING(3, "派单待确认");

    private final int code;
    private final String label;

    DriverStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    /** 是否可参与派单 */
    public boolean canAcceptDispatch() {
        return this == ONLINE_IDLE;
    }

    public static DriverStatus fromCode(int code) {
        for (DriverStatus s : values()) {
            if (s.code == code) return s;
        }
        return OFFLINE; // 兜底
    }
}
