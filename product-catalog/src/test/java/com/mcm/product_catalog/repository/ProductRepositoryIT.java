package com.mcm.product_catalog.repository;

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;

import com.mcm.product_catalog.entity.Product;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DataMongoTest
public class ProductRepositoryIT {

	@Autowired
	private ProductRepository repo;
	
	@AfterEach
	public void tearDown() {
		repo.deleteAll().block();
	}
	
	@Test
	@DisplayName("Given a product with the specified name exists, when findByName is invoked, then return the product")
	void testFindByName() {
		var product = new Product();
		product.setName("name");
		repo.save(product).block();

		StepVerifier.create(repo.findByName("name")).expectNextMatches(p -> p.getName().equals("name"))
				.verifyComplete();
	}
	
	@Test
	@DisplayName("Given a product whit the specified category exists, when findByCategoryIdsContains is invoked, then return only one product")
	void testFindByCategoryIdsWhenProductWithSearchedCategoryExists() {
		var product = new Product();
		product.setCategoryIds(Set.of("1","2"));
		repo.save(product).block();
		StepVerifier.create(repo.findByCategoryIdsContains("2", PageRequest.of(0, 10)))
				.assertNext(prod -> assertThat(prod).isNotNull()).verifyComplete();
	}
	
	@Test
	@DisplayName("Given a product with the specified category does not exist, when findByCategoryIdsContains is invoked, then return no products")
	void testFindByCategoryIdsWhenProductWithSearchedCategoryDoesNotExist() {
		var product = new Product();
		product.setCategoryIds(Set.of("1", "2"));
		repo.save(product).block();
		StepVerifier.create(repo.findByCategoryIdsContains("3", PageRequest.of(0, 10))).expectNextCount(0)
				.verifyComplete();
	}
	
	@Test
	@DisplayName("Given 2 products with the specified category and 1 product with a different category exist, when findByCategoryIdsContains is invoked, then return only 2 products")
	void testFindByCategoryIdsWhenMultipleProductsExistWithSearchedCategory() {
		var product1 = new Product();
		product1.setCategoryIds(Set.of("1", "2"));
		var product2 = new Product();
		product2.setCategoryIds(Set.of("1", "2"));
		var product3 = new Product();
		product3.setCategoryIds(Set.of("3"));
		repo.save(product1).block();
		repo.save(product2).block();
		repo.save(product3).block();
		StepVerifier.create(repo.findByCategoryIdsContains("2", PageRequest.of(0, 10))).expectNextCount(2)
				.verifyComplete();
	}
}
