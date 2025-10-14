package com.aurionpro.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorDetailResponseDto {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;

   
    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
}