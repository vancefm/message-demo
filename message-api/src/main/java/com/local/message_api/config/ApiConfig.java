package com.local.message_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ApiConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
