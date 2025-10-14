package com.aurionpro.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.aurionpro.dto.request.VendorCreateRequestDto;
import com.aurionpro.dto.request.VendorUpdateRequestDto;
import com.aurionpro.dto.response.VendorDetailResponseDto;
import com.aurionpro.dto.response.VendorResponseDto;

public interface VendorService {
	VendorResponseDto createVendor(Authentication authentication, VendorCreateRequestDto dto);

    List<VendorResponseDto> getAllVendors(Authentication authentication);

    VendorDetailResponseDto getVendorById(Authentication authentication, Long vendorId);

    VendorResponseDto updateVendor(Authentication authentication, Long vendorId, VendorUpdateRequestDto dto);

    void deleteVendor(Authentication authentication, Long vendorId);
}
