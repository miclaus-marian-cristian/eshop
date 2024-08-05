package com.mcm.product_catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mcm.product_catalog.entity.Product;

import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String>{

	Mono<Product> findByName(String name);
	
	//method to find products by category
	Mono<Page<Product>> findByCategoryIdsContains(String categoryId, Pageable page);

}
