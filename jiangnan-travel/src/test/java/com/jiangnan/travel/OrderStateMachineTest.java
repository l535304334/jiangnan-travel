package com.jiangnan.travel;

import com.jiangnan.travel.enums.DriverStatus;
import com.jiangnan.travel.enums.OrderStatus;
import com.jiangnan.travel.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态机 + 支付状态 单元测试。
 * ponytail: pure unit tests, no Spring context needed.
 */
@DisplayName("订单状态机 & 支付状态")
class OrderStateMachineTest {

    // ── OrderStatus 流转规则 ──

    @Test
    @DisplayName("合法流转: CREATED → PAID → DRIVER_ASSIGNED → ASSIGNED → ARRIVED → IN_PROGRESS → COMPLETED")
    void validFullFlow() {
        assertTrue(OrderStatus.PAID.canTransitionFrom(OrderStatus.CREATED));
        assertTrue(OrderStatus.DRIVER_ASSIGNED.canTransitionFrom(OrderStatus.PAID));
        assertTrue(OrderStatus.ASSIGNED.canTransitionFrom(OrderStatus.DRIVER_ASSIGNED),
                "新路径: DRIVER_ASSIGNED → ASSIGNED");
        assertTrue(OrderStatus.ARRIVED.canTransitionFrom(OrderStatus.ASSIGNED));
        assertTrue(OrderStatus.IN_PROGRESS.canTransitionFrom(OrderStatus.ARRIVED));
        assertTrue(OrderStatus.COMPLETED.canTransitionFrom(OrderStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("兼容老路径: CREATED → PAID → ASSIGNED (直接)")
    void legacyFlowPaidToAssigned() {
        assertTrue(OrderStatus.ASSIGNED.canTransitionFrom(OrderStatus.PAID),
                "老路径 PAID→ASSIGNED 必须保持兼容");
    }

    @Test
    @DisplayName("DRIVER_ASSIGNED → CANCELLED (派单后可取消)")
    void driverAssignedToCancelled() {
        assertTrue(OrderStatus.CANCELLED.canTransitionFrom(OrderStatus.DRIVER_ASSIGNED));
    }

    @Test
    @DisplayName("合法取消: CREATED/PAID/ASSIGNED/ARRIVED → CANCELLED")
    void validCancellations() {
        assertTrue(OrderStatus.CANCELLED.canTransitionFrom(OrderStatus.CREATED));
        assertTrue(OrderStatus.CANCELLED.canTransitionFrom(OrderStatus.PAID));
        assertTrue(OrderStatus.CANCELLED.canTransitionFrom(OrderStatus.ASSIGNED));
        assertTrue(OrderStatus.CANCELLED.canTransitionFrom(OrderStatus.ARRIVED));
    }

    @Test
    @DisplayName("合法退款: COMPLETED → REFUNDED, CANCELLED → REFUNDED")
    void validRefunds() {
        assertTrue(OrderStatus.REFUNDED.canTransitionFrom(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.REFUNDED.canTransitionFrom(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("非法流转: CREATED → COMPLETED (跳过多步)")
    void illegalSkipSteps() {
        assertFalse(OrderStatus.COMPLETED.canTransitionFrom(OrderStatus.CREATED),
                "不允许从 CREATED 直接跳到 COMPLETED");
    }

    @Test
    @DisplayName("非法流转: ASSIGNED → CREATED (逆向)")
    void illegalReverse() {
        assertFalse(OrderStatus.CREATED.canTransitionFrom(OrderStatus.ASSIGNED),
                "不允许从 ASSIGNED 回退到 CREATED");
    }

    @Test
    @DisplayName("非法流转: COMPLETED → CANCELLED (终态不应取消)")
    void illegalCancelCompleted() {
        assertFalse(OrderStatus.CANCELLED.canTransitionFrom(OrderStatus.COMPLETED),
                "已完成的订单不应被取消");
    }

    @Test
    @DisplayName("非法流转: IN_PROGRESS → PAID (逆向支付)")
    void illegalPayAfterStart() {
        assertFalse(OrderStatus.PAID.canTransitionFrom(OrderStatus.IN_PROGRESS),
                "行程中不应重新支付");
    }

    @Test
    @DisplayName("状态码转换 fromCode/toCode 一致性")
    void codeRoundTrip() {
        for (OrderStatus s : OrderStatus.values()) {
            assertEquals(s, OrderStatus.fromCode(s.getCode()),
                    "fromCode(toCode) should return same enum for " + s);
        }
    }

    @Test
    @DisplayName("fromCode 非法值抛异常")
    void fromCodeInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromCode(99));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromCode(-1));
    }

    // ── PaymentStatus ──

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    @DisplayName("PaymentStatus 所有值都有有效 code/label")
    void paymentStatusHasCodeAndLabel(PaymentStatus s) {
        assertTrue(s.getCode() >= 0, s + " code should be >= 0");
        assertNotNull(s.getLabel(), s + " label should not be null");
        assertFalse(s.getLabel().isEmpty(), s + " label should not be empty");
    }

    @Test
    @DisplayName("PaymentStatus fromCode 往返一致")
    void paymentStatusRoundTrip() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.fromCode(0));
        assertEquals(PaymentStatus.PAID, PaymentStatus.fromCode(1));
        assertEquals(PaymentStatus.FAILED, PaymentStatus.fromCode(2));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.fromCode(3));
    }

    // ── v1.2: DriverStatus ──

    @Test
    @DisplayName("DriverStatus: OFFLINE/BUSY 不可接单, ONLINE_IDLE 可接单")
    void driverStatusCanAccept() {
        assertFalse(DriverStatus.OFFLINE.canAcceptDispatch(), "OFFLINE 不能接单");
        assertTrue(DriverStatus.ONLINE_IDLE.canAcceptDispatch(), "ONLINE_IDLE 可以接单");
        assertFalse(DriverStatus.BUSY.canAcceptDispatch(), "BUSY 不能接单");
        assertFalse(DriverStatus.ASSIGNMENT_PENDING.canAcceptDispatch(), "ASSIGNMENT_PENDING 不能接单");
    }

    @Test
    @DisplayName("DriverStatus: fromCode 往返 + 无效值兜底 OFFLINE")
    void driverStatusRoundTrip() {
        assertEquals(DriverStatus.OFFLINE, DriverStatus.fromCode(0));
        assertEquals(DriverStatus.ONLINE_IDLE, DriverStatus.fromCode(1));
        assertEquals(DriverStatus.BUSY, DriverStatus.fromCode(2));
        assertEquals(DriverStatus.ASSIGNMENT_PENDING, DriverStatus.fromCode(3));
        assertEquals(DriverStatus.OFFLINE, DriverStatus.fromCode(99), "无效code→OFFLINE兜底");
    }
}
