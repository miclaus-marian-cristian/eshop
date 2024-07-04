package com.mcm.category_catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.mcm.category_catalog.entity.Category;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataMongoTest()
public class CategoryRepositoryIT {

	@Autowired
	private CategoryRepository repo;
	
	@AfterEach
	public void tearDown() {
		repo.deleteAll().block();
	}

	@Test
	@DisplayName(value = "Given there is only one top level category in the db \n"
			+ "When CategoryRepository::findByIsTopLevel(true) is invoked \n"
			+ "Then only one category is retrieved")
	public void shouldReturn1TopLevelCategory() {
		var category1 = Category.builder().isTopLevel(true).build();
		var category2 = Category.builder().isTopLevel(false).build();

		repo.saveAll(List.of(category1, category2)).blockLast();
		
		Flux<Category> actualCategories = repo.findByIsTopLevel(true);
		
		 // Log the retrieved categories for debugging
        actualCategories.doOnNext(category -> System.out.println("Retrieved category: " + category)).subscribe();
		
		StepVerifier.create(actualCategories).expectNextMatches(category -> {
            
			assertThat(category.isTopLevel()).as("Expected category to be top-level but it was not. Category: " + category).isTrue();
			return true;
		}).verifyComplete();
		
	}
}
