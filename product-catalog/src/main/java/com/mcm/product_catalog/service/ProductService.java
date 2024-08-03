package com.mcm.product_catalog.service;

import org.springframework.stereotype.Service;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.exception.EntityAlreadyExistsException;
import com.mcm.product_catalog.mapper.ProductMapper;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProductService {

	private final ProductRepository productRepository;

	public Mono<Product> createProduct(CreateProductRequest productRequest) {

		Product product = ProductMapper.toProduct(productRequest);

		return productRepository.findByName(product.getName())
				.flatMap(existingProduct -> Mono.<Product>error(new EntityAlreadyExistsException()))
				.switchIfEmpty(productRepository.save(product));
	}

	// find products by category
	public Mono<Product> findByCategoryIds(String categoryId) {
		return productRepository.findByCategoryIds(categoryId);
	}
}
