package com.mcm.product_catalog.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.pojo.CreateProductRequest;
import com.mcm.product_catalog.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductEndpoint {

	private final ProductService productService;
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<Product> createProduct(@Valid @RequestBody CreateProductRequest product) {
		return productService.createProduct(product);
	}
	
}
