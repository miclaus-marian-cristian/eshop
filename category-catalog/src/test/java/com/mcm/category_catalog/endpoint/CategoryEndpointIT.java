package com.mcm.category_catalog.endpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

import com.mcm.category_catalog.config.httperror.ErrorAttributesConfig;
import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.pojo.CategoryList;
import com.mcm.category_catalog.service.CategoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@Import(ErrorAttributesConfig.class)
public class CategoryEndpointIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CategoryService categoryService;


    @AfterEach
    public void cleanUp() {
        reset(categoryService);
    }

    @Test
    @DisplayName(value = "Given there is only one top level category in the db \n"
			+ "When a GET request is made on /api/categories/top-level \n"
			+ "Then only one category is retrieved")
    public void shouldReturnStatusOkAnd1TopLevelCategory() {
    	
        var category1 = Category.builder().isTopLevel(true).build();
        
        given(categoryService.getAllTopLevel()).willReturn(Flux.just(category1));
        
        webTestClient.get()
            .uri("/api/categories/top-level")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Category.class)
            .hasSize(1)
            .contains(Category.builder().isTopLevel(true).build());
    }
    
    @Test
    @DisplayName(value = "Given there is no top level category in the db \n"
			+ "When a GET request is made on /api/categories/top-level \n"
			+ "Then an empty list is returned "
			+ "And a 200 http status code")
    public void shouldReturnStatusOkAnd0Categories() {
    	given(categoryService.getAllTopLevel()).willReturn(Flux.empty());
    	webTestClient.get()
        .uri("/api/categories/top-level")
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(List.class)
        .hasSize(0);
    }
    
    @Test
    @DisplayName(value = "Given there are categories in the db \n"
			+ "When the id from the GET request made on /api/categories/{id} doesn't match with any category from the db "
			+ "Then a 404 http status code is returned")
    public void shouldReturn404() {
    	given(categoryService.getById(anyString())).willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")));
    	
    	webTestClient.get()
        .uri("/api/categories/any-id")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .consumeWith(response -> {
            String responseBody = new String(response.getResponseBodyContent());
            System.out.println("Response Body: " + responseBody);
        })
        .jsonPath("$.status").isEqualTo(404)
        .jsonPath("$.error").isEqualTo("Resource not found")
        .jsonPath("$.path").isEqualTo("/api/categories/any-id")
        .jsonPath("$.timestamp").exists();
    }
    
    @Test
    @DisplayName(value = "Given there are categories in the db \n"
			+ "When the id from the GET request made on /api/categories/{id} exists in the db "
			+ "Then a 200 http status code is returned")
    public void shouldReturn200() {
    	given(categoryService.getById(anyString())).willReturn(Mono.just(Category.builder().id("1").build()));
    	
    	webTestClient.get()
        .uri("/api/categories/1")
        .exchange()
        .expectStatus().isOk()
        .expectBody(Category.class)
        .value(ctgry -> {
        	assertThat(ctgry.getId()).isEqualTo("1");
        });
    }
    
    @Test
    public void given3CtgsExistWhenTheKeywordMatchesThePrefixOfOnly2CtgsThenReturn2CtgsAndStatusOK() {
    	var cat1 = Category.builder().name("Electrics").build();
    	var cat2 = Category.builder().name("Electronics").build();
    	when(categoryService.getByKeyword("ele")).thenReturn(Mono.just(new CategoryList(List.of(cat1, cat2))));
    	webTestClient.get()
    	.uri("/api/categories/keyword/ele")
    	.exchange()
    	.expectStatus().isOk()
    	.expectBody(CategoryList.class)
    	.value(ctgList -> assertThat(ctgList.getCategories().size()).isEqualTo(2));
    }
    
}
