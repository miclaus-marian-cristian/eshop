package com.mcm.product_catalog.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
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
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.pojo.ProductPage;
import com.mcm.product_catalog.service.ProductService;
import com.mcm.product_catalog.util.KeyPairUtil;
import com.mcm.product_catalog.util.ProductUtils;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
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
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has a null name, then return status code 400 and the correct error response body")
	void testCreateProductWhenProductNameIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(ProductUtils.generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(ProductUtils.generateAttributes(4));
		
		webTestClient.post().uri(ENDPOINT_BASE_URL)
		.contentType(MediaType.APPLICATION_JSON)
		.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN")))
		.bodyValue(invalidProduct)
		.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
		.expectBody().jsonPath("$.detail").isEqualTo("The name field is required");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the price field of the sent Product object is less then one, then return status code 400 and the correct error response body")
	void testCreateProductWhenProductPriceIsLessThanOne() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(0);
		invalidProduct.setDetails(ProductUtils.generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(ProductUtils.generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The price field must be greater than 0");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has details that are null, then return status code 400 and the correct error response body")
	void testCreateProductWhenProductDetailsIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(ProductUtils.generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The details field is required");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has details with less than 50 characters, then return status code 400 and the correct error response body")
	void testCreateProductWhenProductDetailsLengthIsLessThanFifty() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(ProductUtils.generateString(49));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(ProductUtils.generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The details field must contain between 50 and 1000 characters");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object doesn't contain the categoryIds field, then return status code 400 and the correct error response body")
	void testCreateProductWhenProductCategoryIdsIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(ProductUtils.generateString(200));
		invalidProduct.setAttributes(ProductUtils.generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The categoryIds field is required");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has an empty categoryIds field, then return status code 400 and the correct error response body")
	void testCreateProductWhenProductCategoryIdsIsEmpty() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(ProductUtils.generateString(200));
		invalidProduct.setCategoryIds(Set.of());
		invalidProduct.setAttributes(ProductUtils.generateAttributes(4));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("Product must have at least one category");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has null attributes, then return status code 400")
	void testCreateProductWhenProductAttributesIsNull() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(ProductUtils.generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The attributes field is required");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has 3 attributes, then return status code 400")
	void testCreateProductWhenProductAttributesSizeIsLessThanFour() {
		var invalidProduct = new Product();
		invalidProduct.setName("Product Name");
		invalidProduct.setPrice(100);
		invalidProduct.setDetails(ProductUtils.generateString(200));
		invalidProduct.setCategoryIds(Set.of("1"));
		invalidProduct.setAttributes(ProductUtils.generateAttributes(3));
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
				.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST).expectBody().jsonPath("$.detail")
				.isEqualTo("The attributes field must contain between 4 and and 50 attributes");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when the sent Product object has 51 attributes, then return status code 400")
	void testCreateProductWhenProductAttributesSizeIsGreaterThanFifty() {
        var invalidProduct = new Product();
        invalidProduct.setName("Product Name");
        invalidProduct.setPrice(100);
        invalidProduct.setDetails(ProductUtils.generateString(200));
        invalidProduct.setCategoryIds(Set.of("1"));
        invalidProduct.setAttributes(ProductUtils.generateAttributes(51));
        webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(invalidProduct)
                .exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody().jsonPath("$.detail").isEqualTo("The attributes field must contain between 4 and and 50 attributes");
	}
	
	@Test
	@DisplayName("Given user is authenticated and is ADMIN, when createProduct is called, then return status code 201")
	void testCreateProductWhenProductIsValid() {
		
		//create a valid CreateProductRequest
		CreateProductRequest validProduct = new CreateProductRequest();
		validProduct.setName("Product Name");
		validProduct.setPrice(100);
		validProduct.setDetails(ProductUtils.generateString(50));
		validProduct.setCategoryIds(Set.of("1"));
		validProduct.setAttributes(ProductUtils.generateAttributes(4));
		
		when(productService.createProduct(Mockito.any(CreateProductRequest.class))).thenReturn(Mono.just(new Product()));
		
		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(validProduct)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Product.class);
	}
	
	@Test
	@DisplayName("Given user is authenticated, when createProduct is called, then return status code 403")
	void testCreateProductWhenUserIsNotAdmin() {
        //create a valid CreateProductRequest
        CreateProductRequest validProductReq = new CreateProductRequest();
        validProductReq.setName("Product Name");
        validProductReq.setPrice(100);
        validProductReq.setDetails(ProductUtils.generateString(200));
        validProductReq.setCategoryIds(Set.of("1"));
        validProductReq.setAttributes(ProductUtils.generateAttributes(4));
        
		// mock the createProduct method to return a product
		when(productService.createProduct(Mockito.any(CreateProductRequest.class))).thenReturn(Mono.just(new Product()));
		
		// print the result of the createProduct method
	    Mono<Product> productMono = productService.createProduct(validProductReq);
	    System.out.println("testCreateProductWhenUserIsNotAdmin - Method returned: " + productMono);
		
        webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(generateMockToken("USER"))).bodyValue(validProductReq)
                .exchange().expectStatus().isForbidden();
	}
	
	@Test
	@DisplayName("Given user is not authenticated, when createProduct is called, then return status code 401")
	void testCreateProductWhenUserIsNotAuthenticated() {
		// create a valid CreateProductRequest
		CreateProductRequest validProductReq = new CreateProductRequest();
		validProductReq.setName("Product Name");
		validProductReq.setPrice(100);
		validProductReq.setDetails(ProductUtils.generateString(200));
		validProductReq.setCategoryIds(Set.of("1"));
		validProductReq.setAttributes(ProductUtils.generateAttributes(4));

		// mock the createProduct method to return a product
		when(productService.createProduct(Mockito.any(CreateProductRequest.class)))
				.thenReturn(Mono.just(new Product()));

		// print the result of the createProduct method
		Mono<Product> productMono = productService.createProduct(validProductReq);
		System.out.println("testCreateProductWhenUserIsNotAuthenticated - Method returned: " + productMono);

		webTestClient.post().uri(ENDPOINT_BASE_URL).contentType(MediaType.APPLICATION_JSON).bodyValue(validProductReq)
				.exchange().expectStatus().isUnauthorized();
	}

	// test findByCategoryId method with authenticated user
	@Test
	@DisplayName("Given user is authenticated, when findByCategoryId is called, then return status code 200")
	void testFindByCategoryIdWhenUserIsAuthenticated() {
        // mock the findByCategoryIds method to return a product
		when(productService.findByCategoryId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.just(ProductPage.builder().build()));

        webTestClient.post().uri(ENDPOINT_BASE_URL + "/categories/1").contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(generateMockToken("USER"))).bodyValue("1").exchange()
                .expectStatus().isOk().expectBody(List.class).value((products) -> {
                    // assert that the returned list is not empty
                	assertThat(products).isNotEmpty();
                });
    }
	
	// test findByCategoryId method with unauthenticated user
	@Test
	@DisplayName("Given user is not authenticated, when findByCategoryId is called, then return status code 200")
	void testFindByCategoryIdWhenUserIsNotAuthenticated() {
        // mock the findByCategoryIds method to return a product
		when(productService.findByCategoryId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.just(ProductPage.builder().build()));


        webTestClient.post().uri(ENDPOINT_BASE_URL + "/categories/1").contentType(MediaType.APPLICATION_JSON)
                .bodyValue("1").exchange().expectStatus().isOk()
                .expectBody(List.class).value((products) -> {
                    // assert that the returned list is not empty
                	assertThat(products).isNotEmpty();
                });
    }
}
