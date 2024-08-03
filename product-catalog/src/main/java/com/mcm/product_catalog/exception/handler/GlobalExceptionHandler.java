package com.mcm.product_catalog.exception.handler;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import com.mcm.product_catalog.exception.ServiceException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<ResponseEntity<Map<String, Object>>> handleWebExchangeBindException(WebExchangeBindException e, ServerWebExchange exchange) {
		String detail = e.getFieldErrors().stream()
		        .map(fieldError -> fieldError.getDefaultMessage())
		        .collect(Collectors.joining("; "));
		    Map<String, Object> errorAttributes = Map.of("status", HttpStatus.BAD_REQUEST.value(), "detail", detail, "path", exchange.getRequest().getPath().value());
		    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorAttributes));
    }
	
	//handle ServiceException
	@ExceptionHandler(ServiceException.class)
	public Mono<ResponseEntity<Map<String, Object>>> handleServiceException(ServiceException e,
			ServerWebExchange exchange) {
		Map<String, Object> errorAttributes = Map.of("status", e.getHttpStatus(), "detail", e.getDetail(),
				"path", exchange.getRequest().getPath().value());
		return Mono.just(ResponseEntity.status(e.getHttpStatus()).body(errorAttributes));
	}

}
