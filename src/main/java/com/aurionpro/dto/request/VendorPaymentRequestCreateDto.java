package com.aurionpro.dto.request;

import com.aurionpro.constants.PaymentRequestType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
@Data
public class VendorPaymentRequestCreateDto {
	private Long vendorId; // Optional for VENDOR type

    @NotNull
    @Positive
    private Double amount;

    @NotNull
    private String description;

    @NotNull
    private PaymentRequestType requestType;
}
