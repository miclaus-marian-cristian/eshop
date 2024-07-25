package com.mcm.category_catalog.service;

import org.springframework.stereotype.Service;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.pojo.CategoryList;
import com.mcm.category_catalog.pojo.exception.EntityAlreadyExistsException;
import com.mcm.category_catalog.pojo.exception.EntityInvalidException;
import com.mcm.category_catalog.pojo.exception.EntityNotFoundException;
import com.mcm.category_catalog.repository.CategoryRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	
	public Flux<Category> getAllTopLevel() {
		return categoryRepository.findByIsTopLevel(true);
	}
	
	public Mono<Category> getById(String id){
		 return categoryRepository.findById(id).switchIfEmpty(Mono.error(new EntityNotFoundException()));
	}
	
	public Mono<CategoryList> getByKeyword(String keyword){
		return categoryRepository.findByNameIgnoreCaseStartingWith(keyword).collectList().map(CategoryList::new);
	}
	
	public Mono<Category> create(Category ctg) {
		if(ctg.getName() == null) {
			throw new EntityInvalidException("The name field is empty!");
		}
		return categoryRepository.findByNameIgnoreCaseStartingWith(ctg.getName())
		        .switchIfEmpty(categoryRepository.save(ctg))
				.flatMap(ctgry -> Mono.error(new EntityAlreadyExistsException()))
				.collectList()
				.flatMap(list -> Mono.just((Category) list.get(0)));
	}
}
