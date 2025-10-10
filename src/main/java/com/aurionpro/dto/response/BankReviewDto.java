package com.aurionpro.dto.response;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankReviewDto {
    private String accountHolderName;
    private String accountNumberMasked;
    private String bankName;
    private String ifscCode;
    private String branchName;
    private String status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
}
