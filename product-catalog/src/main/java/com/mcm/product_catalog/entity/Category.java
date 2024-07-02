package com.mcm.product_catalog.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public class Category {
    @Id
    private String id;
    private String name;
    private List<String> subcategoryIds;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getSubcategoryIds() {
		return subcategoryIds;
	}
	public void setSubcategoryIds(List<String> subcategoryIds) {
		this.subcategoryIds = subcategoryIds;
	}
    
}
