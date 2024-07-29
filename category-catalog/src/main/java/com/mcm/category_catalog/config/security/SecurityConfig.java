package com.mcm.category_catalog.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http.csrf(csrfSpec -> csrfSpec.disable());
        http.authorizeExchange(exchange -> exchange.pathMatchers(HttpMethod.POST, "/api/categories")
                .hasRole("ADMIN"));
        http.authorizeExchange(exchange -> exchange.anyExchange().permitAll());
        return http.build();
	}
	
}
