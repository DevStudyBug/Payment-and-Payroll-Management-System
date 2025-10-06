package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgRegisterResponseDto {
	private Long orgId;
	private String orgName;
	private String email;
	private String message;
	private String status;
}
