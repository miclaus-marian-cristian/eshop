package com.mcm.product_catalog.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Document(collection = "products")
public class Product {
	
	@Id
    private String id;

    private String name;

    private double price;
	
    private String details;
	
    private Set<String> categoryIds;
	
    private Map<String, String> attributes;

}
