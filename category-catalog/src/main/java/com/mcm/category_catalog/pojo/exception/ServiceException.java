package com.mcm.category_catalog.pojo.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException{

	private static final long serialVersionUID = 1275400243011361886L;
	
	private final String detail;
	private final int httpStatus;
	
	public ServiceException(String detail, int httpStatus) {

		super(detail);
		this.detail = detail;
		this.httpStatus = httpStatus;
	}

}
