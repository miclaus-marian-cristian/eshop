package com.mcm.category_catalog.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<Category> create(@RequestBody Category ctgry){
		return categoryService.create(ctgry);
	}

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
	public Mono<CategoryList> getByKeyword(@PathVariable(name = "keyword") String keyword){
		return categoryService.getByKeyword(keyword);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Category> createCategory(@RequestBody Category category) {
		return categoryService.create(category);
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Category> updateCategory(@PathVariable("id") String id, @RequestBody Category category) {
		return categoryService.updateCategory(id, category);
	}

}
