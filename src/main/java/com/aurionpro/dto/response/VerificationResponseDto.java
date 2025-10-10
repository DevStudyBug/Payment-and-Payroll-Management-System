package com.aurionpro.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationResponseDto {
    private Long employeeId;
    private String message;
    private String currentStatus;
}
