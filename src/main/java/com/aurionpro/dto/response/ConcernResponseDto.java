package com.aurionpro.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcernResponseDto {
    private Long concernId;
    private String employeeName;
    private String organizationName;
    private String ticketNumber;
    private String category;
    private String priority;
    private String description;
    private String status;
    private String adminResponse;
    private String attachmentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
