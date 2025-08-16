package zxf.trace.support.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局异常处理器 - WebFlux版本
 *
 * 处理应用程序中抛出的所有异常，并将它们转换为适当的HTTP响应。
 * 这个处理器适用于整个应用程序，不仅限于特定包。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     *
     * @param ex 抛出的异常
     * @param exchange 当前Web交换
     * @return 包含错误详情的ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
        return Mono.just(new ResponseEntity<>(new ErrorResponse("SERVER_ERROR", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * 处理参数验证相关的异常
     *
     * @param ex 抛出的运行时异常
     * @param exchange 当前Web交换
     * @return 包含错误详情的ResponseEntity
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(RuntimeException ex, ServerWebExchange exchange) {
        return Mono.just(new ResponseEntity<>(new ErrorResponse("BAD_REQUEST", ex.getMessage()),
                HttpStatus.BAD_REQUEST));
    }

    /**
     * 处理资源未找到异常
     *
     * @param ex 抛出的异常
     * @param exchange 当前Web交换
     * @return 包含错误详情的ResponseEntity
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFound(ResourceNotFoundException ex, ServerWebExchange exchange) {
        return Mono.just(new ResponseEntity<>(new ErrorResponse("NOT_FOUND", ex.getMessage()),
                HttpStatus.NOT_FOUND));
    }

    /**
     * 错误响应数据结构
     */
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}