package com.aurionpro.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.aurionpro.entity.DesignationEntity;

public interface DesignationService {
	public DesignationEntity addDesignation(Authentication authentication, String name);

	public List<DesignationEntity> getAllDesignations(Authentication authentication);
}
