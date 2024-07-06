package com.mcm.category_catalog.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.repository.CategoryRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	
	public Flux<Category> getAllTopLevel() {
		return categoryRepository.findByIsTopLevel(true);
	}
	
	public Mono<Category> getById(String id){
		 return categoryRepository.findById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")));
	}
}
