package com.aurionpro.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            // Detect if it's an image or other file
            String contentType = file.getContentType();
            String resourceType = (contentType != null && contentType.startsWith("image"))
                    ? "image"
                    : "raw"; // PDF, DOC, etc. -> raw

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "organization_docs",
                            "public_id", file.getOriginalFilename(),
                            "overwrite", true,
                            "use_filename", true));

            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage());
        }
    }
}
