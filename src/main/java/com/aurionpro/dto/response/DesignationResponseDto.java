package com.aurionpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DesignationResponseDto {
	private Long designationId;
	private String name;

}