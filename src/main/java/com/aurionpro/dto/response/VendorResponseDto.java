package com.aurionpro.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorResponseDto {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String address;
}