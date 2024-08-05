package com.mcm.product_catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.exception.EntityAlreadyExistsException;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.pojo.ProductPage;
import com.mcm.product_catalog.repository.ProductRepository;

import reactor.core.publisher.Flux;
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
	@DisplayName("Given a product with the specified name does not exist, when createProduct is invoked, then return the product")
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
	@DisplayName("Given a product with the specified name exists, when createProduct is invoked, then return an EntityAlreadyExists")
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

	
	@Test
	@DisplayName("Given 2 products with the specified category exists, when findByCategoryId(sortDir:1,sortField:price) is invoked, then return the products ordered by price in asc order")
	void testFindByCategoryIdsWhenProductsExistSortAscByPrice() {
        //mock the repository method findByCategoryIds to return a Mono<Page<Product>>
		
		//create 2 valid products with diff prices
		Product product1 = new Product();
		product1.setPrice(100);
		Product product2 = new Product();
		product2.setPrice(200);
		var products = List.of(product1, product2);

		when(repo.findByCategoryIdsContains(anyString(), Mockito.any())).thenReturn(Mono.just(new PageImpl<>(products)));

        //verify that the product is returned
        StepVerifier.create(productService.findByCategoryId("1", 0, 10, "name", "1"))
				.assertNext(page -> {
					assertThat(page.getProducts().get(0).getPrice()).isLessThan(page.getProducts().get(1).getPrice());
					})
				.verifyComplete();
    }
	
	@Test
	@DisplayName("Given 2 products with the specified category exists, when findByCategoryId(sortDir:1,sortField:price) is invoked, then return the products ordered by price in asc order")
	void testFindByCategoryIdsWhenProductsExistSortDescByPrice() {
        //mock the repository method findByCategoryIds to return a Mono<Page<Product>>
		
		//create 2 valid products with diff prices
		Product product1 = new Product();
		product1.setPrice(100);
		Product product2 = new Product();
		product2.setPrice(200);
		var products = List.of(product1, product2);

		when(repo.findByCategoryIdsContains(anyString(), Mockito.any())).thenReturn(Mono.just(new PageImpl<>(products)));

        //verify that the product is returned
        StepVerifier.create(productService.findByCategoryId("1", 0, 10, "name", "1"))
				.assertNext(page -> {
					assertThat(page.getProducts().get(1).getPrice()).isLessThan(page.getProducts().get(0).getPrice());
					})
				.verifyComplete();
    }

	// test findByCategoryIds method when no products exist for the category
	@Test
	void testFindByCategoryIdsWhenProductsDoNotExist() {
        //mock the repository method findByCategoryIds to return empty Mono
		when(repo.findByCategoryIdsContains(anyString(), Mockito.any())).thenReturn(Mono.empty());
        //verify that no product is returned
        StepVerifier.create(productService.findByCategoryId("1", 0, 10, "name", "1"))
            .expectNextCount(0)
            .verifyComplete();
    }
}
