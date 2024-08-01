package com.mcm.category_catalog.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
@Document
public class Category {
	@Id
	@EqualsAndHashCode.Include
	private String id;
	private String name;
	private boolean isTopLevel;
	private Set<String> subcategoryIds;
	private List<Map<String, String>> productAttributes;
	
}
