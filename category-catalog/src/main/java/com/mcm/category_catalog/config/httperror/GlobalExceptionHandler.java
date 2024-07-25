
package com.mcm.category_catalog.config.httperror;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.mcm.category_catalog.pojo.exception.EntityNotFoundException;
import com.mcm.category_catalog.pojo.exception.ServiceException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(EntityNotFoundException.class)
	public Mono<ResponseEntity<Map<String, Object>>> handleEntityNotFoundException(EntityNotFoundException ex, ServerWebExchange exchange) {
	    Map<String, Object> errorAttributes = new HashMap<>();
	    errorAttributes.put("status", HttpStatus.NOT_FOUND.value());
	    errorAttributes.put("detail", ex.getMessage());
	    errorAttributes.put("path", exchange.getRequest().getPath().value());
	    errorAttributes.put("timestamp", LocalDateTime.now());
	    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorAttributes));
	}

	@ExceptionHandler({Throwable.class})
	public Mono<ResponseEntity<Map<String, Object>>> handleServiceExceptions(Throwable ex, ServerWebExchange exchange) {
		Map<String, Object> errorAttributes = new HashMap<>();
		int httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
		
		if(ex instanceof ServiceException servEx) {
		errorAttributes.put("status", servEx.getHttpStatus());
		errorAttributes.put("detail", servEx.getDetail());
		httpStatus = servEx.getHttpStatus();
		}
		else {
			errorAttributes.put("status", 500 );
			errorAttributes.put("detail", "This exception was handled");
		}
		errorAttributes.put("path", exchange.getRequest().getPath().value());
		errorAttributes.put("timestamp", LocalDateTime.now());
		return Mono.just(ResponseEntity.status(httpStatus).body(errorAttributes));
	}

}
