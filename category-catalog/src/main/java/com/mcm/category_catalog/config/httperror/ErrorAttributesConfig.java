package com.mcm.category_catalog.config.httperror;

import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorAttributesConfig {

	@Bean
    public ErrorAttributes errorAttributes() {
        return new CustomErrorAttributes();
    }
	
}
