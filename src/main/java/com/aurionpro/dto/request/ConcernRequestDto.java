package com.aurionpro.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcernRequestDto {
    private String category;   // PAYROLL, BANK_DETAILS, etc.
    private String priority;   // LOW, MEDIUM, HIGH
    private String description;
}
