package com.mcm.product_catalog.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mcm.product_catalog.entity.Product;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product, String>{

}
