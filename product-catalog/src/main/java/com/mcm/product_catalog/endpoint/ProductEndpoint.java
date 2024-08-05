package com.mcm.product_catalog.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.pojo.ProductPage;
import com.mcm.product_catalog.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductEndpoint {

	private final ProductService productService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEndpoint.class);
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<Product> createProduct(@Valid @RequestBody CreateProductRequest product) {
		LOGGER.info("Received request to create product: {}", product);
		  return productService.createProduct(product)
		    .doOnSuccess(prod -> LOGGER.info("Product created: {}", prod))
		    .doOnError(e -> LOGGER.error("Error creating product: {}", product, e));
	}
	
	@PostMapping("/categories/{categoryId}")
	@ResponseStatus(code = HttpStatus.OK)
	public Mono<ProductPage> findByCategoryId(@RequestBody String categoryId, @RequestParam("pNumb") Integer pageNumber, @RequestParam("pSize") Integer pageSize, @RequestParam("sortField") String sortField, @RequestParam("sortDir") String sortDir) {
        return productService.findByCategoryId(categoryId, pageNumber, pageSize, sortField, sortDir)
        		.doOnNext(prod -> LOGGER.info("Product found: {}", prod))
        		.doOnError(e -> LOGGER.error("Error finding product by category: {}", categoryId, e));
	}
	
}
