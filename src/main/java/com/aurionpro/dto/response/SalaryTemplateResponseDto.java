package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryTemplateResponseDto {

    private Long salaryTemplateId;
    private Long designationId;
    private String designationName;
    private String message;
}
