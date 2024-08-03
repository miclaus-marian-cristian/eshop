package com.mcm.product_catalog.endpoint;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mcm.product_catalog.config.TestSecurityConfig;
import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.exception.handler.GlobalExceptionHandler;
import com.mcm.product_catalog.mapper.ProductMapper;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.service.ProductService;
import com.mcm.product_catalog.util.KeyPairUtil;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@WebFluxTest
@ActiveProfiles("test")
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
public class ProductEndpointIT {
	
	private final String ENDPOINT_BASE_URL = "/api/products";

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private ProductService productService;
	
	private JwtEncoder jwtEncoder;
	
	@AfterEach
	public void cleanUp() {
		reset(productService);
	}

	
	@PostConstruct
	public void init() {

		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		try {
			privateKey = KeyPairUtil.loadPrivateKey();
			publicKey = KeyPairUtil.loadPublicKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) publicKey).privateKey(privateKey).build();
		JWKSet jwkSet = new JWKSet(Collections.singletonList(rsaKey));
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
		this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
	}
	
	private String generateMockToken(String role) {
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject("user")
                .claim("scope", role)
                .claim("authorities", role)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claimsSet)).getTokenValue();
    }
	
	// method to generate a string of specific length
	private String generateString(int length) {
		return new String(new char[length]).replace('\0', 'a');
	}
	
	// method to generate a map of attributes of specific size
	private Map<String, String> generateAttributes(int size) {
		Map<String, String> attributes = new java.util.HashMap<>();
		for (int i = 1; i <= size; i++) {
			attributes.put("key" + i, "value");
		}
		return attributes;
	}
	
	@Test
	void testCreateProductWhenProductNameIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(generateAttributes(4));
		
		webTestClient.post().uri(ENDPOINT_BASE_URL)
		.contentType(MediaType.APPLICATION_JSON)
		.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN")))
		.bodyValue(invalidProduct)
		.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
		.expectBody().jsonPath("$.detail").isEqualTo("The name field is required");
	}
	
	@Test
	void testCreateProductWhenProductPriceIsLessThanOne() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(0);
		invalidProduct.setDetails(generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The price field must be greater than 0");
	}
	
	@Test
	void testCreateProductWhenProductDetailsIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The details field is required");
	}
	
	@Test
	void testCreateProductWhenProductDetailsLengthIsLessThanFifty() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(generateString(49));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The details field must contain between 50 and 1000 characters");
	}
	
	@Test
	void testCreateProductWhenProductCategoryIdsIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(generateString(200));
		invalidProduct.setAttributes(generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The categoryIds field is required");
	}
	
	@Test
	void testCreateProductWhenProductCategoryIdsIsEmpty() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(generateString(200));
		invalidProduct.setCategoryIds(Set.of());
		invalidProduct.setAttributes(generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("Product must have at least one category");
	}
	
	@Test
	void testCreateProductWhenProductAttributesIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The attributes field is required");
	}
	
	@Test
	void testCreateProductWhenProductAttributesSizeIsLessThanFour() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(generateAttributes(3));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The attributes field must contain between 4 and and 50 attributes");
	}
	
	@Test
	void testCreateProductWhenProductAttributesSizeIsGreaterThanFifty() {
        var invalidProduct = new Product();
        invalidProduct.setName("Product Name");
        invalidProduct.setPrice(100);
        invalidProduct.setDetails(generateString(200));
        invalidProduct.setCategoryIds(Set.of("1"));
        invalidProduct.setAttributes(generateAttributes(51));
        webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
                .exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody().jsonPath("$.detail").isEqualTo("The attributes field must contain between 4 and and 50 attributes");
	}
	
	@Test
	void testCreateProductWhenProductIsValid() {
		//create a valid CreateProductRequest
		CreateProductRequest validProduct = new CreateProductRequest();
		validProduct.setName("Product Name");
		validProduct.setPrice(100);
		validProduct.setDetails(generateString(200));
		validProduct.setCategoryIds(Set.of("1"));
		validProduct.setAttributes(generateAttributes(4));
		
		when(productService.createProduct(validProduct)).thenReturn(Mono.just(ProductMapper.toProduct(validProduct)));
		
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(validProduct)
				.exchange().expectStatus().isCreated().expectBody(Product.class)
				.value(product -> product.getName().equals(validProduct.getName()));
	}

}
