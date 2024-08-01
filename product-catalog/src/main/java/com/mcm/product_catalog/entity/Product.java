package com.mcm.product_catalog.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "products")
public class Product {
	
	@Id
	@EqualsAndHashCode.Include
    private String id;
    private String name;
    private double price;
    private String details;
    private List<String> categoryIds;
    private List<Map<String, String>> attributes;

}
