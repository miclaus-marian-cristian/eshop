package com.mcm.category_catalog.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.pojo.CategoryList;
import com.mcm.category_catalog.service.CategoryService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
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
	
	@GetMapping("/keyword/{keyword}")
	@ResponseStatus(code = HttpStatus.OK)
	public Mono<CategoryList> getByKeyword(@PathVariable("keyword") String keyword){
		return categoryService.getByKeyword(keyword);
	}

}
