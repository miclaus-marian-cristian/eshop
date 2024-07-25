package com.mcm.category_catalog.pojo.exception;

import org.springframework.http.HttpStatus;

public class EntityAlreadyExistsException extends ServiceException{

	private static final long serialVersionUID = 5475958138462852592L;
	
	public EntityAlreadyExistsException() {
		super("Resource already exists!", HttpStatus.CONFLICT.value());
	}

}
