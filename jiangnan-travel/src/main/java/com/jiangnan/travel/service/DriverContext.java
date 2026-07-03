package com.jiangnan.travel.service;

/**
 * 司机运行时上下文 — 评分引擎的额外输入。
 * ponytail: POJO, no persistence needed.
 */
public class DriverContext {

    /** 空闲时间(分钟) — 自上次完成订单到现在的时长 */
    private long idleMinutes;

    /** 累计拒单次数 */
    private int rejectionCount;

    /** 当前时间戳 */
    private long nowMillis;

    public DriverContext() {}

    public DriverContext(long idleMinutes, int rejectionCount, long nowMillis) {
        this.idleMinutes = idleMinutes;
        this.rejectionCount = rejectionCount;
        this.nowMillis = nowMillis;
    }

    public long getIdleMinutes() { return idleMinutes; }
    public int getRejectionCount() { return rejectionCount; }
    public long getNowMillis() { return nowMillis; }

    public void setIdleMinutes(long v) { this.idleMinutes = v; }
    public void setRejectionCount(int v) { this.rejectionCount = v; }
    public void setNowMillis(long v) { this.nowMillis = v; }
}
