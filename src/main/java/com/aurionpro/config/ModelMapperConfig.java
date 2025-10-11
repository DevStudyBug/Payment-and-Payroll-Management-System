package com.aurionpro.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aurionpro.dto.response.ConcernResponseDto;
import com.aurionpro.entity.ConcernEntity;

@Configuration
public class ModelMapperConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		// Optional: stricter matching to avoid accidental mappings
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true)
				.setAmbiguityIgnored(true);

		
		return modelMapper;
	}
}
