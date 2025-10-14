package com.aurionpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectRequestDto {
    @NotBlank(message = "Rejection reason is required.")
    private String reason;
}