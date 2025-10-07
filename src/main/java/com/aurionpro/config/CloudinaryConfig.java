package com.aurionpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dmyolzjvi",
                "api_key", "839383949732634",
                "api_secret","f2u5rfgPaAjujdL7QYtn3JY5PBg",
                "secure", true
        ));
    }
}
