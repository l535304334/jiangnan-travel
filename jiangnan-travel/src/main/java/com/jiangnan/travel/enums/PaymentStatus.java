package com.jiangnan.travel.enums;

/**
 * 支付状态流转：pending → paid / failed → refunded。
 */
public enum PaymentStatus {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    FAILED(2, "支付失败"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String label;

    PaymentStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PaymentStatus fromCode(int code) {
        for (PaymentStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown PaymentStatus code: " + code);
    }
}
