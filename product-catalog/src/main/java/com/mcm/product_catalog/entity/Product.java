package com.mcm.product_catalog.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "products")
public class Product {
	
	@Id
    private String id;
    private String name;
    private double price;
    private String details;
    private List<String> categoryIds;
    private Map<String, String> attributes;

}
