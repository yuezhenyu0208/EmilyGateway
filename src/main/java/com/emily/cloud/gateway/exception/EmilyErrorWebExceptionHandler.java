package com.emily.cloud.gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.cloud.gateway.filter.factory.RetryGatewayFilterFactory.RETRY_ITERATION_KEY;

/**
 * @program: EmilyGateway
 * @description: 自定义异常处理
 * @create: 2021/01/06
 */
public class EmilyErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(EmilyErrorWebExceptionHandler.class);
    private final ErrorAttributes errorAttributes;

    public EmilyErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.errorAttributes = errorAttributes;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        return super.handle(exchange, throwable);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(request, options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE));

        if (!options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
            errorAttributes.remove("exception");
        }

        if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
            errorAttributes.remove("trace");
        }

        if (!options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && errorAttributes.get("message") != null) {
            errorAttributes.put("message", "");
        }

        if (!options.isIncluded(ErrorAttributeOptions.Include.BINDING_ERRORS)) {
            errorAttributes.remove("errors");
        }
        errorAttributes.remove("timestamp");
        errorAttributes.remove("path");
        errorAttributes.put("message", org.apache.commons.lang3.StringUtils.join(errorAttributes.get("error"),
                ",", errorAttributes.get("message"),
                ",", errorAttributes.get("exception"),
                ",", "重试次数：" + request.exchange().getAttributeOrDefault(RETRY_ITERATION_KEY, 0)));
        errorAttributes.remove("exception");
        errorAttributes.remove("error");
        errorAttributes.remove("requestId");
        return errorAttributes;
    }

    @Override
    public ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
        ErrorAttributeOptions options = ErrorAttributeOptions.of(new ErrorAttributeOptions.Include[]{
                ErrorAttributeOptions.Include.EXCEPTION,
                ErrorAttributeOptions.Include.MESSAGE});
        return options;
    }

    @Override
    public void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        String message = throwable.getMessage();
        logger.error(org.apache.commons.lang3.StringUtils.join(message, "\n\r", PrintExceptionInfo.printErrorInfo(throwable), "\n\r", PrintExceptionInfo.printErrorInfo(throwable.getSuppressed())));
    }

}
