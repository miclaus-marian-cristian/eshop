package com.mcm.product_catalog.mapper;

import com.mcm.product_catalog.entity.Product;
import com.mcm.product_catalog.pojo.CreateProductRequest;

public class ProductMapper {

	/**
	 * Convert CreateProductRequest to Product
	 * @param request
	 * @return Product
	 */
	public static Product toProduct(CreateProductRequest request) {
		return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .details(request.getDetails())
                .categoryIds(request.getCategoryIds())
                .attributes(request.getAttributes())
                .build();
	}
}
