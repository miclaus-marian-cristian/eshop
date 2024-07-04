package com.mcm.category_catalog.service;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	
	public Flux<Category> getAllTopLevel() {
		return categoryRepository.findByIsTopLevel(true);
	}
}
