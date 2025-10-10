package com.aurionpro.service;



import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dto.request.EmployeeRegisterRequestDto;
import com.aurionpro.dto.request.LoginRequestDto;
import com.aurionpro.dto.request.OrgRegisterRequestDto;
import com.aurionpro.dto.response.EmployeeBulkRegisterResponseDto;
import com.aurionpro.dto.response.EmployeeRegisterResponseDto;
import com.aurionpro.dto.response.LoginResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;
import com.aurionpro.entity.OrganizationEntity;

public interface AuthService {
	public OrgRegisterResponseDto registerOrganization(OrgRegisterRequestDto req);

	public LoginResponseDto loginOrganization(LoginRequestDto req);

	public EmployeeRegisterResponseDto registerEmployee(Authentication authentication,
			EmployeeRegisterRequestDto request);
	public EmployeeBulkRegisterResponseDto registerEmployeesInBulk(OrganizationEntity org,
	        List<EmployeeRegisterRequestDto> employeeRequests);
	public EmployeeBulkRegisterResponseDto registerEmployeesFromExcel(Authentication authentication,
			MultipartFile file);
}
