package com.mcm.category_catalog.pojo;

import java.util.List;

import com.mcm.category_catalog.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryList {
	
	private List<Category> categories;

}
