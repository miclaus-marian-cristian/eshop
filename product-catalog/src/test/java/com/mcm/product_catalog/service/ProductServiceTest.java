package com.mcm.product_catalog.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.exception.EntityAlreadyExistsException;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.repository.ProductRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ProductServiceTest {

	@Mock
	private ProductRepository repo;

	@InjectMocks
	private ProductService productService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	// test createProduct method when product does not exist
	@Test
	void testCreateProductWhenNameIsUnique() {
		//mock the repository method findByName to return empty Mono
		when(repo.findByName(anyString())).thenReturn(Mono.empty());
		when(repo.save(Mockito.any())).thenReturn(Mono.just(new Product()));
		var request = new CreateProductRequest();
		request.setName(anyString());
		StepVerifier.create(productService.createProduct(request))
            .expectNextMatches(product -> product != null)
            .verifyComplete();
	}

	// test createProduct method when product already exists
	@Test
	void testCreateProductWhenNameIsNotUnique() {
        //mock the repository method findByName to return a Mono
        when(repo.findByName(anyString())).thenReturn(Mono.just(new Product()));
        when(repo.save(Mockito.any())).thenReturn(Mono.just(new Product()));
        var request = new CreateProductRequest();
		request.setName("name");
        StepVerifier.create(productService.createProduct(request))
            .expectError(EntityAlreadyExistsException.class)
            .verify();
    }

	// test findByCategoryIds method when products exist for the category
	@Test
	void testFindByCategoryIdsWhenProductsExist() {
        //mock the repository method findByCategoryIds to return a Mono
        when(repo.findByCategoryIds(anyString())).thenReturn(Mono.just(new Product()));
        //verify that the product is returned
        StepVerifier.create(productService.findByCategoryIds("1"))
            .expectNextMatches(product -> product != null)
            .verifyComplete();
    }

	// test findByCategoryIds method when no products exist for the category
	@Test
	void testFindByCategoryIdsWhenProductsDoNotExist() {
        //mock the repository method findByCategoryIds to return empty Mono
        when(repo.findByCategoryIds(anyString())).thenReturn(Mono.empty());
        //verify that no product is returned
        StepVerifier.create(productService.findByCategoryIds("1"))
            .expectNextCount(0)
            .verifyComplete();
    }
}
