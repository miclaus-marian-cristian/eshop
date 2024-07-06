package com.mcm.category_catalog.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.service.CategoryService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/categories")
public class CategoryEndpoint {

	private final CategoryService categoryService;

	@GetMapping("/top-level")
	public Flux<Category> getTopLevelCategories() {
		return categoryService.getAllTopLevel();
	}
	
	@GetMapping(path = "/{id}")
	public Mono<Category> getById(@PathVariable("id") String id){
		return categoryService.getById(id);
	}

}
