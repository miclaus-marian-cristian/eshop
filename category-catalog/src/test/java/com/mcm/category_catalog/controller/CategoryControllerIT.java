package com.mcm.category_catalog.controller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.service.CategoryService;

import reactor.core.publisher.Flux;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;

import java.util.List;

@WebFluxTest(properties = {"eureka.client.enabled=false"})
public class CategoryControllerIT {

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
			+ "Then only one category is retrieved")
    public void shouldReturnStatusOkAnd0Categories() {
    	given(categoryService.getAllTopLevel()).willReturn(Flux.empty());
    	webTestClient.get()
        .uri("/api/categories/top-level")
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(List.class)
        .hasSize(0);
    }
}
