package com.mcm.product_catalog.service;

import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.exception.EntityAlreadyExistsException;
import com.mcm.product_catalog.mapper.ProductMapper;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.pojo.ProductPage;
import com.mcm.product_catalog.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProductService {

	private final ProductRepository productRepository;

	public Mono<Product> createProduct(CreateProductRequest productRequest) {

		Product product = ProductMapper.toProduct(productRequest);
		
		// capitalize every first letter of every word of the product name
		String name = product.getName().toLowerCase();
		Pattern pattern = Pattern.compile("(^|\\s|\\-|\\.|\\_|\\+)([a-z])");
		var matcher = pattern.matcher(name);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
		    matcher.appendReplacement(sb, matcher.group(1) + matcher.group(2).toUpperCase());
		}
		matcher.appendTail(sb);

		product.setName(sb.toString());
		
		return productRepository.findByName(product.getName())
				.flatMap(existingProduct -> Mono.<Product>error(new EntityAlreadyExistsException()))
				.switchIfEmpty(productRepository.save(product));
	}

	// find products by category
	public Mono<ProductPage> findByCategoryId(String categoryId, Integer pageNumber, Integer pageSize, String sortField, String sortDir) {

		// Determine the direction of the sort
	    Direction direction = sortDir.equalsIgnoreCase("1") ? Direction.ASC : Direction.DESC;

	    // Create a Sort object
	    Sort sort = Sort.by(direction, sortField);

	    // Create a Pageable object
	    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);
	    
	    return productRepository.findByCategoryIdsContains(categoryId, pageRequest)
	            .flatMap(page -> Mono.just(ProductPage.builder()
	                .products(page.getContent())
	                .pageNumber(page.getNumber())
	                .pageSize(page.getSize())
	                .totalPages(page.getTotalPages())
	                .totalElements(page.getTotalElements())
	                .build()));
	    
	}
		
}
