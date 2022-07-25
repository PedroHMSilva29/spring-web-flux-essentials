package br.com.pehenmo.springwebfluxessentials.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static org.springframework.boot.web.error.ErrorAttributeOptions.defaults;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webproperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer codecConfigurer) {

        super(errorAttributes, webproperties.getResources(), applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);
    }

    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {

        ErrorAttributeOptions errorAttributeOptions = isTraceEnabled(request) ?
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE) :
                defaults();

        Map<String, Object> errorAttributesMap = getErrorAttributes(request, errorAttributeOptions);
        int status = (int) Optional.ofNullable(errorAttributesMap.get("status")).orElse(500);

        Throwable throwable = getError(request);
        if(throwable instanceof ResponseStatusException){
            throwable = (ResponseStatusException) throwable;
            errorAttributesMap.put("message", throwable.getMessage());
            errorAttributesMap.put("developmentMessage", "a custom ResponseStatusException");
        }


        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorAttributesMap));
    }

    @Override
    protected boolean isTraceEnabled(ServerRequest request) {
        if(null != request.uri().getQuery() &&
                request.uri().getQuery().contains("trace=true"))
            return true;
        return false;
    }
}
