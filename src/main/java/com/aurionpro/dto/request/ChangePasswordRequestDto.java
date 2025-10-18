package com.aurionpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
	@NotBlank(message = "Email is required")
    private String email;

	@NotBlank(message = "Old password is required")
	private String oldPassword;

	@NotBlank(message = "New password is required")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
	         message = "Password must contain uppercase, lowercase, number, and special character")
	private String newPassword;
}
