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
        int status = response.getStatus();
        boolean isError = status != HttpStatus.OK.value();

        // 定义日志记录函数
        BiConsumer<Logger, String> logger = (log, message) -> {
            if (isError) {
                log.error(message);
            } else if (log.isDebugEnabled()) {
                log.debug(message);
            }
        };

        // 定义日志内容生成函数
        Supplier<String> requestLog = () -> String.join("\n",
                "=================================================Request begin(Inbound)=================================================",
                String.format("URI             : %s", request.getRequestURI()),
                String.format("Method          : %s", request.getMethod()),  // 修正拼写错误
                String.format("Headers         : %s", formatHeaders(Collections.list(request.getHeaderNames()),
                        headerName -> request.getHeader(headerName))),
                String.format("Request Body    : %s", readAndMaskContent(request.getContentAsByteArray(),
                        request.getCharacterEncoding())),
                "=================================================Request end(Inbound)================================================="
        );

        Supplier<String> responseLog = () -> String.join("\n",
                "=================================================Response begin(Inbound)=================================================",
                String.format("Status code     : %d", status),
                String.format("Headers         : %s", formatHeaders(response.getHeaderNames(),
                        response::getHeader)),
                String.format("Response Body   : %s", readAndMaskContent(response.getContentAsByteArray(),
                        response.getCharacterEncoding())),
                "=================================================Response end(Inbound)================================================="
        );

        // 执行日志记录
        logger.accept(log, requestLog.get());
        logger.accept(log, responseLog.get());
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
