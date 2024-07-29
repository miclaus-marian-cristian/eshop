package com.mcm.category_catalog.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import reactor.core.publisher.Mono;

@TestConfiguration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class TestSecurityConfig {

	@Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrfSpec -> csrfSpec.disable());
        http.authorizeExchange(exchange -> exchange
                .pathMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                .anyExchange().permitAll())
        .oauth2ResourceServer((oauth2ResourceServer) ->
        oauth2ResourceServer
            .jwt((jwt) ->
						jwt
						.jwtDecoder(jwtDecoder())
						.jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
    ) // Ensure JWT is used
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable); // Disable basic authentication;
        return http.build();
    }

	@Bean
    @Primary
    ReactiveJwtDecoder jwtDecoder() {
        return token -> {
            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject("user")
                .claim("scope", "ADMIN") // Note: No ROLE_ prefix
                .claim("authorities", "ROLE_ADMIN") // Ensure 'ROLE_' prefix for roles
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
            
            return Mono.just(Jwt.withTokenValue(token)
                .headers(headers -> headers.put("alg", "RS256"))
                .claims(claims -> claims.putAll(claimsSet.getClaims()))
                .build());
        };
    }

	@Bean
	@Primary
	JwtEncoder jwtEncoder() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey privateKey = pair.getPrivate();
		PublicKey publicKey = pair.getPublic();

		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) publicKey).privateKey(privateKey).build();
		JWKSet jwkSet = new JWKSet(Collections.singletonList(rsaKey));
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
		return new NimbusJwtEncoder(jwkSource);
	}

    @Bean
    Converter<Jwt, Mono<JwtAuthenticationToken>> jwtAuthenticationConverter() {
    	JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            List<String> authorities = jwt.getClaimAsStringList("authorities");
            if (authorities == null) {
                authorities = Collections.emptyList();
            }
            return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        };
    }
    
    private static class ReactiveJwtAuthenticationConverterAdapter implements Converter<Jwt, Mono<JwtAuthenticationToken>> {
        private final JwtAuthenticationConverter jwtAuthenticationConverter;

        public ReactiveJwtAuthenticationConverterAdapter(JwtAuthenticationConverter jwtAuthenticationConverter) {
            this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        }

        @Override
        public Mono<JwtAuthenticationToken> convert(Jwt jwt) {
            return Mono.justOrEmpty((JwtAuthenticationToken)jwtAuthenticationConverter.convert(jwt));
        }
    }

}
