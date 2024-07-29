package com.mcm.category_catalog.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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

import com.mcm.category_catalog.config.TestSecurityConfig;
import com.mcm.category_catalog.config.httperror.GlobalExceptionHandler;
import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.pojo.CategoryList;
import com.mcm.category_catalog.pojo.exception.EntityAlreadyExistsException;
import com.mcm.category_catalog.pojo.exception.EntityInvalidException;
import com.mcm.category_catalog.pojo.exception.EntityNotFoundException;
import com.mcm.category_catalog.service.CategoryService;
import com.mcm.category_catalog.util.KeyPairUtil;
//import com.mcm.category_catalog.utils.KeyPairUtil;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
@ActiveProfiles("test")
public class CategoryEndpointIT {

	private final String ENDPOINT_BASE_URL = "/api/categories";

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private CategoryService categoryService;
	
	private JwtEncoder jwtEncoder;

	
	@PostConstruct
	public void init() {

		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		try {
			privateKey = KeyPairUtil.loadPrivateKey();
			publicKey = KeyPairUtil.loadPublicKey();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				KeyPairUtil.generateAndStoreKeyPair();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) publicKey).privateKey(privateKey).build();
		JWKSet jwkSet = new JWKSet(Collections.singletonList(rsaKey));
		JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
		this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
	}
	 

	@AfterEach
	public void cleanUp() {
		reset(categoryService);
	}

	@Test
	@DisplayName(value = "Given there is only one top level category in the db \n"
			+ "When a GET request is made on /api/categories/top-level \n" + "Then only one category is retrieved")
	public void shouldReturnStatusOkAnd1TopLevelCategory() {

		var category1 = Category.builder().isTopLevel(true).build();

		given(categoryService.getAllTopLevel()).willReturn(Flux.just(category1));

		webTestClient.get().uri(ENDPOINT_BASE_URL + "/top-level").exchange().expectStatus().isOk()
				.expectBodyList(Category.class).hasSize(1).contains(Category.builder().isTopLevel(true).build());
	}

	@Test
	@DisplayName(value = "Given there is no top level category in the db \n"
			+ "When a GET request is made on /api/categories/top-level \n" + "Then an empty list is returned "
			+ "And a 200 http status code")
	public void shouldReturnStatusOkAnd0Categories() {
		given(categoryService.getAllTopLevel()).willReturn(Flux.empty());
		webTestClient.get().uri(ENDPOINT_BASE_URL + "/top-level").exchange().expectStatus().isOk()
				.expectBodyList(List.class).hasSize(0);
	}

	@Test
	@DisplayName(value = "Given there are categories in the db \n"
			+ "When the id from the GET request made on /api/categories/{id} doesn't match with any category from the db "
			+ "Then a 404 http status code is returned")
	public void shouldReturn404() {
		given(categoryService.getById("1")).willThrow(new EntityNotFoundException());

		webTestClient.get().uri(ENDPOINT_BASE_URL + "/1").exchange().expectStatus().isNotFound().expectBody()
				.consumeWith(response -> {
					String responseBody = new String(response.getResponseBodyContent());
					System.out.println("Response Body: " + responseBody);
				}).jsonPath("$.status").isEqualTo(404).jsonPath("$.detail").isEqualTo("Resource not found!")
				.jsonPath("$.path").isEqualTo("/api/categories/1").jsonPath("$.timestamp").exists();
	}

	@Test
	@DisplayName(value = "Given there are categories in the db \n"
			+ "When the id from the GET request made on /api/categories/{id} exists in the db "
			+ "Then a 200 http status code is returned")
	public void shouldReturn200() {
		given(categoryService.getById(anyString())).willReturn(Mono.just(Category.builder().id("1").build()));

		webTestClient.get().uri(ENDPOINT_BASE_URL + "/1").exchange().expectStatus().isOk().expectBody(Category.class)
				.value(ctgry -> {
					assertThat(ctgry.getId()).isEqualTo("1");
				});
	}

	@Test
	public void given3CtgsExistWhenTheKeywordMatchesThePrefixOfOnly2CtgsThenReturn2CtgsAndStatusOK() {
		var cat1 = Category.builder().name("Electrics").build();
		var cat2 = Category.builder().name("Electronics").build();
		when(categoryService.getByKeyword("ele")).thenReturn(Mono.just(new CategoryList(List.of(cat1, cat2))));
		webTestClient.get().uri("/api/categories/keyword/ele").exchange().expectStatus().isOk()
				.expectBody(CategoryList.class)
				.value(ctgList -> assertThat(ctgList.getCategories().size()).isEqualTo(2));
	}

	@Test
	public void givenANewCtgryIsBeingAddedWhenTheNewCtgrysNameIsDifferentFromOtherOnesThenReturn201AndTheCreatedCtgry() {
		var ctgryBeingAdded = Category.builder().name("Electronics").build();
		when(categoryService.create(ctgryBeingAdded)).thenReturn(Mono.just(ctgryBeingAdded));

		webTestClient.post().uri(ENDPOINT_BASE_URL)
		.headers(headers -> headers.setBearerAuth(generateMockToken("ROLE_ADMIN")))
		.contentType(MediaType.APPLICATION_JSON).bodyValue(ctgryBeingAdded)
				.exchange().expectStatus().isCreated().expectBody(Category.class)
				.value(ctgry -> assertThat(ctgry.getName()).isEqualTo(ctgryBeingAdded.getName()));

	}

	@Test
	public void testCreateCategoryWhenTheCtgrysNameAlreadyExists() {
		var ctgryBeingAdded = Category.builder().name("Electronics").build();
		when(categoryService.create(ctgryBeingAdded)).thenThrow(new EntityAlreadyExistsException());

		webTestClient.post()
		.uri(ENDPOINT_BASE_URL)
		.contentType(MediaType.APPLICATION_JSON)
		.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN")))
		.bodyValue(ctgryBeingAdded)
		.exchange().expectStatus().isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	public void testCreateCategoryWhenTheCtgrysNameIsNull() {
		var ctgryBeingAdded = Category.builder().build();
		when(categoryService.create(ctgryBeingAdded)).thenThrow(new EntityInvalidException("The name field is empty!"));

		webTestClient.post().uri(ENDPOINT_BASE_URL)
		.contentType(MediaType.APPLICATION_JSON)
		.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN")))
		.bodyValue(ctgryBeingAdded)
		.exchange().expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
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
	public void testUpdateCategoryWhenTheCategoryDoesNotExist() {
		var ctgryBeingUpdated = Category.builder().name("Electronics").build();
		when(categoryService.updateCategory("1", ctgryBeingUpdated)).thenThrow(new EntityNotFoundException());

		webTestClient.patch().uri(ENDPOINT_BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(ctgryBeingUpdated)
				.exchange().expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	public void testUpdateCategoryWhenTheCategoryExists() {
        var ctgryBeingUpdated = Category.builder().name("Electronics").build();
        when(categoryService.updateCategory("1", ctgryBeingUpdated)).thenReturn(Mono.just(ctgryBeingUpdated));

        webTestClient.patch().uri(ENDPOINT_BASE_URL + "/1").contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(generateMockToken("ADMIN"))).bodyValue(ctgryBeingUpdated)
                .exchange().expectStatus().isOk().expectBody(Category.class)
                .value(ctgry -> assertThat(ctgry.getName()).isEqualTo(ctgryBeingUpdated.getName()));
    }

}
