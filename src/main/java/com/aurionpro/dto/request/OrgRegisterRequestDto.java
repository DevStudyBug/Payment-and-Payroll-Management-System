package com.aurionpro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgRegisterRequestDto {

	@NotBlank(message = "Organization name is required.")
	@Size(min = 3, max = 100, message = "Organization name must be between 3 and 100 characters.")
	private String orgName;

	@NotBlank(message = "Registration number is required.")
	@Size(min = 5, max = 20, message = "Registration number must be between 5 and 20 characters.")
	private String registrationNo;

	@NotBlank(message = "Address cannot be empty.")
	@Size(min = 10, message = "Address must be at least 10 characters long.")
	private String address;

	@NotBlank(message = "Contact number is required.")
	@Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits.")
	private String contactNo;

	@NotBlank(message = "Email is required.")
	@Email(message = "Please enter a valid email address.")
	private String email;
	
	@NotBlank(message = "username is required.")
	@Size(min = 4, max = 10, message = "username name must be between 4 and 10 characters.")
	private String username;
	

	@NotBlank(message = "Password is required.")
	@Size(min = 8, message = "Password must be at least 8 characters long.")
	private String password;
}
