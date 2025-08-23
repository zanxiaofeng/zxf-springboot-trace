package zxf.trace.support.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.info("Handling exception: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse("SERVER_ERROR", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public record ErrorResponse(String code, String message) {
    }
}
