package com.mcm.category_catalog.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.core.publisher.Mono;

public class ReactiveJwtAuthenticationConverterAdapter implements Converter<Jwt, Mono<JwtAuthenticationToken>> {
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public ReactiveJwtAuthenticationConverterAdapter(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    public Mono<JwtAuthenticationToken> convert(Jwt jwt) {
        return Mono.justOrEmpty((JwtAuthenticationToken)jwtAuthenticationConverter.convert(jwt));
    }
}
