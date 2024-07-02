package com.mcm.category_catalog.entity;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Category {
	@Id
	private UUID id;
	private String name;
	private Set<UUID> subcategoryIds;
	private Set<String> productAttributes;
}
