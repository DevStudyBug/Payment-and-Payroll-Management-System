package com.aurionpro.dto.request;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendorCreateRequestDto {

	@NotBlank(message = "Vendor name is required")
	@Size(min = 2, max = 100, message = "Vendor name must be between 2 and 100 characters")
	private String name;

	@Size(max = 100, message = "Contact person name must not exceed 100 characters")
	private String contactPerson;

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Size(max = 100, message = "Email must not exceed 100 characters")
	private String email;
	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^[+]?[0-9]{10}$", message = "Phone number must be 10 digits, optionally starting with +")
	private String phoneNumber;

	 @NotBlank(message = "Address is required")
	    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
	    private String address;
	    
	    @NotBlank(message = "Bank name is required")
	    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
	    private String bankName;
	    
	    @NotBlank(message = "Bank account number is required")
	    @Pattern(
	        regexp = "^[0-9]{12}$",
	        message = "Bank account number must be 12 digits"
	    )
	    private String bankAccountNumber;
	    
	    @NotBlank(message = "IFSC code is required")
	    @Pattern(
	        regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
	        message = "IFSC code must be in format: 4 letters, '0', then 6 alphanumeric characters (e.g., SBIN0001234)"
	    )
	    private String ifscCode;
}