package com.aurionpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankDetailsRequestDto {
    @NotBlank(message = "Account holder name is required.")
    private String accountHolderName;

    @NotBlank(message = "Account number is required.")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Account number must be 9-18 digits.")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required.")
    private String ifscCode;

    @NotBlank(message = "Bank name is required.")
    private String bankName;
}
