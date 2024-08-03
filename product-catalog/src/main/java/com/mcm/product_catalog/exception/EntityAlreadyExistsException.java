package com.mcm.product_catalog.exception;

import org.springframework.http.HttpStatus;

public class EntityAlreadyExistsException extends ServiceException{

	private static final long serialVersionUID = 1159127673415095352L;

	public EntityAlreadyExistsException() {
		super("Resource already exists!", HttpStatus.CONFLICT.value());
	}
}
