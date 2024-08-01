package com.mcm.category_catalog.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

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
        http.oauth2ResourceServer((oauth2ResourceServer) ->
        oauth2ResourceServer
        .jwt((jwt) ->
					jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
        )
) // Ensure JWT is used
    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable); 
        return http.build();
	}
    
    @Bean
    Converter<Jwt, Mono<JwtAuthenticationToken>> jwtAuthenticationConverter() {
    	JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
	
}
