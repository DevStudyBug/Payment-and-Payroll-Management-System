package com.aurionpro.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryTemplateDetailResponseDto {
    private Long salaryTemplateId;
    private String designation;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal da;
    private BigDecimal pf;
    private BigDecimal otherAllowances;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private String createdAt;
    private String updatedAt;
}
