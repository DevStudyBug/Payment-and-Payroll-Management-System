package com.aurionpro.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgReviewRequestDto {
    @NotNull
    private Boolean approved;

    private String remarks;
}
