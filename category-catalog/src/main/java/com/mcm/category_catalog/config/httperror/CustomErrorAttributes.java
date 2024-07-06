package com.mcm.category_catalog.config.httperror;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Throwable error = getError(request);
        if (error instanceof ResponseStatusException) {
            ResponseStatusException ex = (ResponseStatusException) error;
            errorAttributes.put("status", ex.getStatusCode().value());
            errorAttributes.put("error", ex.getReason());
            errorAttributes.put("path", request.path());
            errorAttributes.put("timestamp", LocalDateTime.now());
        }
        return errorAttributes;
    }
}
