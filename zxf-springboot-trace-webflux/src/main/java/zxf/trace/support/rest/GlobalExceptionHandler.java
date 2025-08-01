package zxf.trace.support.rest;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 - WebFlux版本
 *
 * 处理应用程序中抛出的所有异常，并将它们转换为适当的HTTP响应。
 * 这个处理器适用于整个应用程序，不仅限于特定包。
 */
@Component
@Order(-2) // 高优先级，确保在默认错误处理器之前执行
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    /**
     * 构造函数
     *
     * @param errorAttributes 错误属性
     * @param webProperties Web属性
     * @param applicationContext 应用程序上下文
     * @param serverCodecConfigurer 服务器编解码器配置器
     */
    public GlobalExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties.Resources resources,
            ApplicationContext applicationContext,
            ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageReaders(serverCodecConfigurer.getReaders());
        this.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    /**
     * 配置路由规则
     *
     * @param routes 路由函数构建器
     * @return 路由函数
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 渲染错误响应
     *
     * @param request 服务器请求
     * @return 服务器响应
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        Map<String, Object> errorResponse = new HashMap<>();

        HttpStatus status;
        String errorCode;

        if (error instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "NOT_FOUND";
        } else if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "BAD_REQUEST";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "SERVER_ERROR";
        }

        errorResponse.put("code", errorCode);
        errorResponse.put("message", error.getMessage());

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}
