package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiangnan.travel.common.BusinessException;
import com.jiangnan.travel.common.ErrorCode;
import com.jiangnan.travel.dto.ApplyInvoiceRequest;
import com.jiangnan.travel.entity.Invoice;
import com.jiangnan.travel.entity.Order;
import com.jiangnan.travel.mapper.InvoiceMapper;
import com.jiangnan.travel.mapper.OrderMapper;
import com.jiangnan.travel.service.InvoiceService;
import com.jiangnan.travel.service.NotificationService;
import com.jiangnan.travel.vo.InvoiceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceMapper invoiceMapper;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceVO apply(ApplyInvoiceRequest request, Long userId) {
        // 1. 校验订单
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != 4) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "仅已完成订单可申请发票");
        }

        // 2. 检查是否已申请
        Invoice existing = invoiceMapper.selectOne(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getOrderId, request.getOrderId())
                        .eq(Invoice::getUserId, userId)
                        .last("LIMIT 1"));
        if (existing != null) {
            if (existing.getStatus() == 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已提交发票申请，请勿重复提交");
            }
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该订单已开具发票");
        }

        // 3. 创建发票记录
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setOrderId(request.getOrderId());
        invoice.setTitle(request.getTitle());
        invoice.setTaxNo(request.getTaxNo());
        invoice.setAmount(order.getFinalPrice());
        invoice.setStatus(0);
        invoiceMapper.insert(invoice);

        notificationService.create(userId, "INVOICE_APPLIED",
                "发票申请已提交",
                "订单 " + order.getOrderNo() + " 的发票申请已提交，金额 ¥" + order.getFinalPrice(),
                request.getOrderId());

        log.info("用户[{}]申请发票，订单[{}]，抬头[{}]", userId, request.getOrderId(), request.getTitle());
        return toVO(invoice, order);
    }

    @Override
    public InvoiceVO getById(Long invoiceId, Long userId) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null || !invoice.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "发票不存在");
        }
        Order order = orderMapper.selectById(invoice.getOrderId());
        return toVO(invoice, order);
    }

    @Override
    public List<InvoiceVO> listByUser(Long userId) {
        List<Invoice> invoices = invoiceMapper.selectList(
                new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getUserId, userId)
                        .orderByDesc(Invoice::getCreateTime));
        return invoices.stream().map(inv -> {
            Order order = orderMapper.selectById(inv.getOrderId());
            return toVO(inv, order);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long invoiceId, Long userId) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null || !invoice.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "发票不存在");
        }
        if (invoice.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅待开具的发票可取消");
        }
        invoice.setStatus(2); // status 2 = 已取消
        invoiceMapper.updateById(invoice);
        log.info("用户[{}]取消发票[{}]", userId, invoiceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoiceVO issue(Long invoiceId) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) throw new BusinessException(ErrorCode.NOT_FOUND, "发票不存在");
        if (invoice.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅待开具的发票可开具");
        }

        String invoiceNo = "INV" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
        invoice.setInvoiceNo(invoiceNo);
        invoice.setStatus(1);
        invoiceMapper.updateById(invoice);

        notificationService.create(invoice.getUserId(), "INVOICE_ISSUED",
                "发票已开具", "发票 " + invoiceNo + " 已开具，金额 ¥" + invoice.getAmount(), invoice.getOrderId());

        log.info("发票[{}]已开具，编号[{}]", invoiceId, invoiceNo);
        Order order = orderMapper.selectById(invoice.getOrderId());
        return toVO(invoice, order);
    }

    private InvoiceVO toVO(Invoice invoice, Order order) {
        if (invoice == null) return null;
        return InvoiceVO.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrderId())
                .orderNo(order != null ? order.getOrderNo() : null)
                .invoiceNo(invoice.getInvoiceNo())
                .title(invoice.getTitle())
                .taxNo(invoice.getTaxNo())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .statusText(invoice.getStatus() == 0 ? "申请中" : invoice.getStatus() == 1 ? "已开具" : "已取消")
                .createTime(invoice.getCreateTime())
                .startAddress(order != null ? order.getStartAddress() : null)
                .endAddress(order != null ? order.getEndAddress() : null)
                .build();
    }
}
