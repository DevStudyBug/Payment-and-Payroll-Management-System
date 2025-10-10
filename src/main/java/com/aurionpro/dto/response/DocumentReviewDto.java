package com.aurionpro.dto.response;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentReviewDto {
    private Long documentId;
    private String type;
    private String fileName;
    private String fileUrl;
    private String status;
    private String rejectionReason;
    private LocalDateTime uploadedAt;
    private String actionRequired;
}
