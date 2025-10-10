package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayrollGenerateResponseDto {
    private String message;
    private String month;
    private int totalEmployees;
}
