package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.ApplyInvoiceRequest;
import com.jiangnan.travel.vo.InvoiceVO;

import java.util.List;

public interface InvoiceService {

    InvoiceVO apply(ApplyInvoiceRequest request, Long userId);

    InvoiceVO getById(Long invoiceId, Long userId);

    List<InvoiceVO> listByUser(Long userId);

    void cancel(Long invoiceId, Long userId);

    InvoiceVO issue(Long invoiceId);
}
