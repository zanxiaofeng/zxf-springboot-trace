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

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundLoggingFilter extends OncePerRequestFilter {
    private final SensitiveDataHelper sensitiveDataHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logRequestAndResponse(requestWrapper, responseWrapper);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        Consumer<String> logger = response.getStatus() != HttpStatus.OK.value() ? log::error : log::info;

        logger.accept("=================================================Request begin(Inbound)=================================================");
        logger.accept(String.format("URL             : %s?%s", request.getRequestURI(), request.getQueryString()));
        logger.accept(String.format("Method          : %s", request.getMethod()));
        logger.accept(String.format("Headers         : %s", formatHeaders(Collections.list(request.getHeaderNames()), request::getHeader)));
        logger.accept(String.format("Request Body    : %s", readAndMaskJsonContent(request.getContentType(), request.getContentAsByteArray(), request.getCharacterEncoding())));
        logger.accept("=================================================Request end(Inbound)=================================================");

        logger.accept("=================================================Response begin(Outbound)=================================================");
        logger.accept(String.format("Status code     : %d", response.getStatus()));
        logger.accept(String.format("Headers         : %s", formatHeaders(response.getHeaderNames(), response::getHeader)));
        logger.accept(String.format("Response Body   : %s", readAndMaskJsonContent(response.getContentType(), response.getContentAsByteArray(), response.getCharacterEncoding())));
        logger.accept("=================================================Response end(Outbound)=================================================");
    }

    private String formatHeaders(Collection<String> headerNames, Function<String, String> headerValueProvider) {
        Function<String, String> sensitiveHeaderFormatProviderWrapper = headerName -> {
            String headerValue = sensitiveDataHelper.isSensitiveHeader(headerName) ? "******" : headerValueProvider.apply(headerName);
            return String.format("%s: %s", headerName, headerValue);
        };

        return headerNames.stream().map(sensitiveHeaderFormatProviderWrapper).collect(Collectors.joining(", ", "[", "]"));
    }

    private String readAndMaskJsonContent(String contentType, byte[] contentBytes, String encoding) {
        try {
            String contentString = new String(contentBytes, encoding);
            if (contentString.isEmpty() || !contentType.contains("json")) {
                return contentString;
            }
            return sensitiveDataHelper.maskSensitiveDataFromJson(contentString);
        } catch (IOException e) {
            log.error("Failed to read content", e);
            return "Content read error";
        }
    }
}
