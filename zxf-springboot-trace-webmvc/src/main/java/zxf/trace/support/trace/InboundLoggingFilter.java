package zxf.trace.support.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
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

        // 记录请求日志
        logRequest(requestWrapper);

        try {
            // 执行过滤链
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 记录响应日志
            logResponse(requestWrapper, responseWrapper);
            // 复制响应内容到原始响应
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        // 根据响应状态码决定日志级别，但在请求阶段我们还不知道响应状态码
        // 所以先使用DEBUG级别，后续在logResponse中根据状态码调整
        if (log.isDebugEnabled()) {
            log.debug("=================================================Request begin(Inbound)=================================================");
            log.debug("URI             : {}", request.getRequestURI());
            log.debug("Methed          : {}", request.getMethod());
            log.debug("Headers         : {}", Collections.list(request.getHeaderNames()).stream()
                    .map(headerName -> String.format("%s:\"%s\"", headerName, request.getHeader(headerName)))
                    .collect(Collectors.toList()));

            // 获取请求体
            String requestBody = "";
            try {
                requestBody = new String(request.getContentAsByteArray(), request.getCharacterEncoding());
                if (!requestBody.isEmpty()) {
                    // 处理敏感数据
                    requestBody = sensitiveDataMasker.maskSensitiveData(requestBody);
                }
            } catch (IOException e) {
                log.error("Failed to read request body", e);
            }
            log.debug("Request Body    : {}", requestBody);
            log.debug("=================================================Request end(Inbound)=================================================");
        }
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        int status = response.getStatus();
        // 根据状态码决定日志级别
        boolean isError = status != 200;

        // 如果是错误状态码，使用ERROR级别，否则使用DEBUG级别
        if (isError) {
            // 如果之前使用DEBUG级别记录了请求，现在需要用ERROR级别重新记录
            log.error("=================================================Request begin(Inbound)=================================================");
            log.error("URI             : {}", request.getRequestURI());
            log.error("Methed          : {}", request.getMethod());
            log.error("Headers         : {}", Collections.list(request.getHeaderNames()).stream()
                    .map(headerName -> String.format("%s:\"%s\"", headerName, request.getHeader(headerName)))
                    .collect(Collectors.toList()));

            // 获取请求体
            String requestBody = "";
            try {
                requestBody = new String(request.getContentAsByteArray(), request.getCharacterEncoding());
                if (!requestBody.isEmpty()) {
                    // 处理敏感数据
                    requestBody = sensitiveDataMasker.maskSensitiveData(requestBody);
                }
            } catch (IOException e) {
                log.error("Failed to read request body", e);
            }
            log.error("Request Body    : {}", requestBody);
            log.error("=================================================Request end(Inbound)=================================================");

            // 记录响应
            log.error("=================================================Response begin(Inbound)=================================================");
            log.error("Status code     : {}", status);
            log.error("Status text     : {}", response.getStatus());
            log.error("Headers         : {}", response.getHeaderNames().stream()
                    .map(headerName -> String.format("%s:\"%s\"", headerName, response.getHeader(headerName)))
                    .collect(Collectors.toList()));

            // 获取响应体
            String responseBody = "";
            try {
                responseBody = new String(response.getContentAsByteArray(), response.getCharacterEncoding());
                if (!responseBody.isEmpty()) {
                    // 处理敏感数据
                    responseBody = sensitiveDataMasker.maskSensitiveData(responseBody);
                }
            } catch (IOException e) {
                log.error("Failed to read response body", e);
            }
            log.error("Response Body   : {}", responseBody);
            log.error("=================================================Response end(Inbound)=================================================");
        } else if (log.isDebugEnabled()) {
            // 使用DEBUG级别记录响应
            log.debug("=================================================Response begin(Inbound)=================================================");
            log.debug("Status code     : {}", status);
            log.debug("Status text     : {}", response.getStatus());
            log.debug("Headers         : {}", response.getHeaderNames().stream()
                    .map(headerName -> String.format("%s:\"%s\"", headerName, response.getHeader(headerName)))
                    .collect(Collectors.toList()));

            // 获取响应体
            String responseBody = "";
            try {
                responseBody = new String(response.getContentAsByteArray(), response.getCharacterEncoding());
                if (!responseBody.isEmpty()) {
                    // 处理敏感数据
                    responseBody = sensitiveDataMasker.maskSensitiveData(responseBody);
                }
            } catch (IOException e) {
                log.error("Failed to read response body", e);
            }
            log.debug("Response Body   : {}", responseBody);
            log.debug("=================================================Response end(Inbound)=================================================");
        }
    }
}
