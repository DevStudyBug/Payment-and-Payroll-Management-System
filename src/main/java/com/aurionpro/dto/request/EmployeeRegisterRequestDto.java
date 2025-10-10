package com.aurionpro.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRegisterRequestDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Past(message = "Date of birth must be in the past.")
    @NotNull(message = "Date of birth is required.")
    private LocalDate dob;

    @NotBlank
    private String department;

    @NotBlank
    private String designation;

    @Email
    @NotBlank
    private String email;
}
