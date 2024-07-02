package com.mcm.category_catalog.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mcm.category_catalog.entity.Category;

@Repository
public interface CategoryRepository extends ReactiveMongoRepository<Category, UUID>{
	
	

}
