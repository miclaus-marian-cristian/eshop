package com.mcm.category_catalog.pojo.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends ServiceException{

	private static final long serialVersionUID = 6841145346494447760L;
	
	public EntityNotFoundException() {
		super("Resource not found!", HttpStatus.NOT_FOUND.value());
	}
}
