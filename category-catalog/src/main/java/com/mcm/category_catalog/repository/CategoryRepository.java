package com.mcm.category_catalog.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.mcm.category_catalog.entity.Category;

import reactor.core.publisher.Flux;

@Repository
public interface CategoryRepository extends ReactiveMongoRepository<Category, String>{
	
	public Flux<Category> findByIsTopLevel(boolean isTopLevel);
	
	public Flux<Category> findByNameIgnoreCaseStartingWith(String prefix);

}
