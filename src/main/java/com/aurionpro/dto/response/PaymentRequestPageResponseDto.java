package com.aurionpro.dto.response;

import java.util.List;

import com.aurionpro.dto.request.PaymentRequestListDto;
import com.aurionpro.dto.request.PaymentRequestSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestPageResponseDto {
    private List<PaymentRequestListDto> content;
    private PaymentRequestSummaryDto summary;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;
}
