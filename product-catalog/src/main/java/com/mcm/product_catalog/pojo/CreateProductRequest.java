package com.mcm.product_catalog.pojo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

	@NotNull(message = "The name field is required")
	@Size(min = 3, max = 50, message = "The name field must contain between 3 and 50 characters")
    private String name;

	@Range(min = 1, message = "The price field must be greater than 0")
    private double price;
	
	@NotNull(message = "The details field is required")
	@Length(min = 50, max = 1000, message = "The details field must contain between 50 and 1000 characters")
    private String details;
	
	@NotNull(message = "The categoryIds field is required")
	@Size(min =1, message = "Product must have at least one category")
    private Set<String> categoryIds;
	
	@NotNull(message = "The attributes field is required")
	@Size(min = 4, max = 50, message = "The attributes field must contain between 4 and and 50 attributes")
    private Map<String, String> attributes;
	
}
