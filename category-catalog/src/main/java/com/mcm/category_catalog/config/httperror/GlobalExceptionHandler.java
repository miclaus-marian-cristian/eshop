
package com.mcm.category_catalog.config.httperror;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import com.mcm.category_catalog.pojo.exception.ServiceException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ServiceException.class)
	public Mono<ResponseEntity<Map<String, Object>>> handleEntityNotFoundException(ServiceException ex, ServerWebExchange exchange) {
	    Map<String, Object> errorAttributes = new HashMap<>();
	    errorAttributes.put("status", ex.getHttpStatus());
	    errorAttributes.put("detail", ex.getDetail());
	    errorAttributes.put("path", exchange.getRequest().getPath().value());
	    errorAttributes.put("timestamp", LocalDateTime.now());
	    return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(errorAttributes));
	}
}
