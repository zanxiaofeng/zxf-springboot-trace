package zxf.trace.support.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundLoggingFilter extends OncePerRequestFilter {

    private final SensitiveDataMasker sensitiveDataMasker;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 包装请求和响应，以便能够多次读取内容
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // 执行过滤链
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 根据响应状态码记录请求和响应日志
            logRequestAndResponse(requestWrapper, responseWrapper);
            // 复制响应内容到原始响应
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        // 定义日志记录函数
        Consumer<String> logger = response.getStatus() != HttpStatus.OK.value() ? log::error : log::info;

        logger.accept("=================================================Request begin(Inbound)=================================================");
        logger.accept(String.format("URI             : %s", request.getRequestURI()));
        logger.accept(String.format("Method          : %s", request.getMethod()));
        logger.accept(String.format("Headers         : %s", formatHeaders(Collections.list(request.getHeaderNames()), headerName -> request.getHeader(headerName))));
        logger.accept(String.format("Request Body    : %s", readAndMaskContent(request.getContentAsByteArray(), request.getCharacterEncoding())));
        logger.accept("=================================================Request end(Inbound)=================================================");


        logger.accept("=================================================Response begin(Outbound)=================================================");
        logger.accept(String.format("Status code     : %d", response.getStatus()));
        logger.accept(String.format("Headers         : %s", formatHeaders(response.getHeaderNames(),
                response::getHeader)));
        logger.accept(String.format("Response Body   : %s", readAndMaskContent(response.getContentAsByteArray(),
                response.getCharacterEncoding())));
        logger.accept("=================================================Response end(Outbound)=================================================");
    }

    // 辅助方法：格式化头部信息
    private String formatHeaders(Collection<String> headerNames, Function<String, String> headerValueProvider) {
        return headerNames.stream()
                .map(headerName -> String.format("%s:\"%s\"", headerName, headerValueProvider.apply(headerName)))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    // 辅助方法：读取并掩码内容
    private String readAndMaskContent(byte[] content, String encoding) {
        try {
            String contentStr = new String(content, encoding);
            return contentStr.isEmpty() ? "" : sensitiveDataMasker.maskSensitiveData(contentStr);
        } catch (IOException e) {
            log.error("Failed to read content", e);
            return "";
        }
    }
}
