package zxf.trace.support.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 入站请求日志过滤器
 * 
 * 这个过滤器拦截所有入站HTTP请求，记录请求和响应的详细信息，
 * 包括URI、方法、头部和正文内容。同时处理敏感数据的掩码，
 * 确保日志中不会暴露敏感信息。
 * 
 * 该过滤器使用Spring的OncePerRequestFilter确保每个请求只被处理一次。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InboundLoggingFilter extends OncePerRequestFilter {

    private final SensitiveDataHelper sensitiveDataHelper;

    /**
     * 过滤器内部处理方法
     * 
     * 包装请求和响应以便能够多次读取内容，执行过滤链，
     * 然后记录请求和响应的详细信息。
     * 
     * @param request 原始HTTP请求
     * @param response 原始HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException 如果发生Servlet异常
     * @throws IOException 如果发生I/O异常
     */
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

    /**
     * 记录请求和响应日志
     * 
     * 根据响应状态码决定日志级别（成功为INFO，错误为ERROR），
     * 记录请求和响应的详细信息，包括URI、方法、头部和正文内容。
     * 
     * @param request 缓存内容的请求包装器
     * @param response 缓存内容的响应包装器
     */
    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        // 定义日志记录函数，根据响应状态码决定日志级别
        Consumer<String> logger = response.getStatus() != HttpStatus.OK.value() ? log::error : log::info;

        logger.accept("=================================================Request begin(Inbound)=================================================");
        logger.accept(String.format("URI             : %s", request.getRequestURI()));
        logger.accept(String.format("Method          : %s", request.getMethod()));
        logger.accept(String.format("Headers         : %s", formatHeaders(Collections.list(request.getHeaderNames()), request::getHeader)));
        logger.accept(String.format("Request Body    : %s", readAndMaskContent(request.getContentAsByteArray(), request.getCharacterEncoding())));
        logger.accept("=================================================Request end(Inbound)=================================================");

        logger.accept("=================================================Response begin(Outbound)=================================================");
        logger.accept(String.format("Status code     : %d", response.getStatus()));
        logger.accept(String.format("Headers         : %s", formatHeaders(response.getHeaderNames(), response::getHeader)));
        logger.accept(String.format("Response Body   : %s", readAndMaskContent(response.getContentAsByteArray(), response.getCharacterEncoding())));
        logger.accept("=================================================Response end(Outbound)=================================================");
    }

    /**
     * 格式化HTTP头部信息
     * 
     * 将头部名称和值格式化为字符串，同时掩码敏感头部的值。
     * 
     * @param headerNames 头部名称集合
     * @param headerValueProvider 头部值提供函数
     * @return 格式化后的头部信息字符串
     */
    private String formatHeaders(Collection<String> headerNames, Function<String, String> headerValueProvider) {
        Function<String, String> headerValueProviderWrapper = headerName -> {
            String headerValue = headerValueProvider.apply(headerName);
            return sensitiveDataHelper.isSensitiveHeader(headerName) ? "******" : headerValue;
        };

        return headerNames.stream().map(headerName -> String.format("%s: %s", headerName, headerValueProviderWrapper.apply(headerName))).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 读取并掩码内容
     * 
     * 将字节数组内容转换为字符串，并对JSON内容中的敏感数据进行掩码处理。
     * 
     * @param content 内容字节数组
     * @param encoding 字符编码
     * @return 掩码处理后的内容字符串
     */
    private String readAndMaskContent(byte[] content, String encoding) {
        try {
            String contentStr = new String(content, encoding);
            return contentStr.isEmpty() ? "" : sensitiveDataHelper.maskSensitiveDataFromJson(contentStr);
        } catch (IOException e) {
            log.error("Failed to read content", e);
            return "";
        }
    }
}
