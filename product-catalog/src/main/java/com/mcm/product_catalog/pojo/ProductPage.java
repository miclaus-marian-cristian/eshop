package com.mcm.product_catalog.pojo;

import java.util.List;
import com.mcm.product_catalog.entity.Product;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class ProductPage {

	private final long totalElements;
	private final int totalPages;
	private final int pageNumber;
	private final int pageSize;
	private final List<Product> products;
	
}
