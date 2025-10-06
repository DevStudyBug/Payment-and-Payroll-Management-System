package com.aurionpro.service;

import com.aurionpro.dto.request.LoginRequestDto;
import com.aurionpro.dto.request.OrgRegisterRequestDto;
import com.aurionpro.dto.response.LoginResponseDto;
import com.aurionpro.dto.response.OrgRegisterResponseDto;

public interface AuthService {
	public OrgRegisterResponseDto registerOrganization(OrgRegisterRequestDto req);

	public LoginResponseDto loginOrganization(LoginRequestDto req);
}
