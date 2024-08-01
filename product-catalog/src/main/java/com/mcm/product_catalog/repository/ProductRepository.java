package com.mcm.product_catalog.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.mcm.product_catalog.entity.Product;

public interface ProductRepository extends ReactiveMongoRepository<Product, String>{

}
