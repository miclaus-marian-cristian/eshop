package com.mcm.category_catalog.pojo.exception;

import org.springframework.http.HttpStatus;

public class EntityInvalidException extends ServiceException{

	private static final long serialVersionUID = 3069995624050042176L;
	

	public EntityInvalidException(String reason) {
		super(reason, HttpStatus.BAD_REQUEST.value());
	}

}
