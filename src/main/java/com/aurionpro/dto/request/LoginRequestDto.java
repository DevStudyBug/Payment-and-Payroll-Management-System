package com.aurionpro.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class LoginRequestDto {
	@NotBlank(message = "userName is required.")
	private String userName;

	@NotBlank(message = "Password is required.")
	private String password;
}
