package com.mcm.category_catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mcm.category_catalog.entity.Category;
import com.mcm.category_catalog.pojo.exception.EntityAlreadyExistsException;
import com.mcm.category_catalog.pojo.exception.EntityNotFoundException;
import com.mcm.category_catalog.repository.CategoryRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CategoryServiceTest {
	
	@Mock
	private CategoryRepository repo;

	@InjectMocks
	private CategoryService categoryService;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	@DisplayName(value = "Given there is two top level categories in the db \n"
			+ "When the CategoryService::getAllTopLevel() is invoked \n"
			+ "Then 2 categories are returned")
	public void shouldReturn2TopLevelCategories() {
		var category1 = Category.builder().isTopLevel(true).build();
		var category2 = Category.builder().isTopLevel(true).build();
		when(repo.findByIsTopLevel(true)).thenReturn(Flux.fromIterable(List.of(category1,category2)));
		var actualCategories = categoryService.getAllTopLevel();
		StepVerifier.create(actualCategories).expectNext(category1).expectNext(category2).verifyComplete();
	}
	
	@Test
	@DisplayName(value = "Given there is just one top level category out of two in the db \n"
			+ "When the CategoryService::getAllTopLevel() is invoked \n"
			+ "Then only one category is returned")
	public void shouldReturn1TopLevelCategory() {
		var category1 = Category.builder().isTopLevel(true).build();
		when(repo.findByIsTopLevel(true)).thenReturn(Flux.fromIterable(List.of(category1)));
		var actualCategories = categoryService.getAllTopLevel();
		StepVerifier.create(actualCategories).expectNext(category1).verifyComplete();
	}
	
	@Test
	@DisplayName("Given there are categories in the db "
			+ "When the requested category id does not exist "
			+ "Then a ResponseStatusException is thrown having the status 404")
	public void shouldThrowAEntityNotFoundException() {
		
		when(repo.findById("1")).thenReturn(Mono.empty());
		
		StepVerifier.create(categoryService.getById("1"))
		.expectErrorSatisfies(throwable -> {
			assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
		}).verify();
	}
	
	@Test
	@DisplayName("Given there are categories in the db "
			+ "When the requested category id exists "
			+ "Then a category is returned")
	public void shouldReturn1CategoryByID() {
		var category = Category.builder().id("1").build();
		when(repo.findById("1")).thenReturn(Mono.just(category));
		
		StepVerifier.create(categoryService.getById("1")).expectNext(category).verifyComplete();
	}
	
	@Test
	public void givenThereAre2CtgsWhen2CtgsNamePrefixMatchesTheKeywordThenReturn2Ctgs() {
		var electronicDevicesCtgry = Category.builder().name("Electronic Devices").build();
		var electricGuitarsCtgry = Category.builder().name("Electric Guitars").build();
		var list = List.of(electronicDevicesCtgry, electricGuitarsCtgry);
		when(repo.findByNameIgnoreCaseStartingWith("ele")).thenReturn(Flux.fromIterable(list));
		
		StepVerifier.create(categoryService.getByKeyword("ele")).expectNextCount(1).consumeNextWith(ctgList -> assertThat(ctgList.getCategories().size()).isEqualTo(2));
	}
	
	@Test
	public void givenANewCtgryIsBeingAddedWhenTheNewCtgrysNameAlreadyExistsThenThrowEntityAlreadyExistsException() {
		var electricGuitarsCtgry = Category.builder().name("Electric Guitars").build();
		when(repo.findByNameIgnoreCaseStartingWith("Electric Guitars")).thenReturn(Flux.just(electricGuitarsCtgry));
		when(repo.save(electricGuitarsCtgry)).thenReturn(Mono.just(electricGuitarsCtgry));
		
		StepVerifier.create(categoryService.create(electricGuitarsCtgry)).expectError(EntityAlreadyExistsException.class).verify();
		
	}
}
