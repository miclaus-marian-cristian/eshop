package com.mcm.product_catalog.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException{

	private static final long serialVersionUID = 1010428790333943718L;
	private final String detail;
	private final int httpStatus;
	
	public ServiceException(String detail, int httpStatus) {

		super(detail);
		this.detail = detail;
		this.httpStatus = httpStatus;
	}

}
