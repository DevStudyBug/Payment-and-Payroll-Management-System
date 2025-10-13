package com.aurionpro.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dto.request.VendorCreateRequestDto;
import com.aurionpro.dto.request.VendorUpdateRequestDto;
import com.aurionpro.dto.response.VendorDetailResponseDto;
import com.aurionpro.dto.response.VendorResponseDto;
import com.aurionpro.service.VendorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/org/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PostMapping
    public ResponseEntity<VendorResponseDto> createVendor(
            Authentication authentication,
            @Valid @RequestBody VendorCreateRequestDto dto) {
        return new ResponseEntity<>(vendorService.createVendor(authentication, dto), HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping
    public ResponseEntity<List<VendorResponseDto>> getAllVendors(Authentication authentication) {
        return ResponseEntity.ok(vendorService.getAllVendors(authentication));
    }
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorDetailResponseDto> getVendorById(
            Authentication authentication,
            @PathVariable Long vendorId) {
        return ResponseEntity.ok(vendorService.getVendorById(authentication, vendorId));
    }
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PutMapping("/{vendorId}")
    public ResponseEntity<VendorResponseDto> updateVendor(
            Authentication authentication,
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpdateRequestDto dto) {
        return ResponseEntity.ok(vendorService.updateVendor(authentication, vendorId, dto));
    }
    @PreAuthorize("hasRole('ORG_ADMIN')")
    @DeleteMapping("/{vendorId}")
    public ResponseEntity<Map<String, String>> deleteVendor(Authentication authentication, @PathVariable Long vendorId) {
        vendorService.deleteVendor(authentication, vendorId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Vendor deleted successfully");

        return ResponseEntity.ok(response);
    }

}
