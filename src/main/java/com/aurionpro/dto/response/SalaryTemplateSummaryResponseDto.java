package com.aurionpro.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SalaryTemplateSummaryResponseDto {
    private Long templateId;
    private String designation;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
}
