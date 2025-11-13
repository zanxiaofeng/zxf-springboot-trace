package zxf.trace;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientResponseException;
import zxf.trace.http.BufferingClientHttpResponseWrapper;
import zxf.trace.sensitive.SensitiveDataHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Autowired
    private SensitiveDataHelper sensitiveDataHelper;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            ClientHttpResponse response = new BufferingClientHttpResponseWrapper(execution.execute(request, body));
            logRequestAndResponse(request, body, response.getStatusCode(), response.getHeaders(), StreamUtils.copyToByteArray(response.getBody()));
            return response;
        } catch (RestClientResponseException ex) {
            log.error("Exception when send request", ex);
            logRequestAndResponse(request, body, ex.getStatusCode(), ex.getResponseHeaders(), ex.getResponseBodyAsByteArray());
            throw ex;
        } catch (Exception ex) {
            log.error("Exception when send request", ex);
            logRequestAndResponse(request, body, null, null, null);
            throw ex;
        }
    }

    private void logRequestAndResponse(HttpRequest request, byte[] requestBody, HttpStatusCode statusCode, HttpHeaders responseHeaders, byte[] responseBody) throws IOException {
        try {
            boolean isError = statusCode == null || statusCode.isError();

            boolean loggingEnabled = isError ? log.isErrorEnabled() : log.isInfoEnabled();
            if (!loggingEnabled) {
                return;
            }

            Consumer<String> logger = isError ? log::error : log::info;
            logger.accept("=================================================Request begin(Outbound)=================================================");
            logger.accept("URL             : " + request.getURI());
            logger.accept("Methed          : " + request.getMethod());
            logger.accept("Headers         : " + formatHeaders(request.getHeaders()));
            logger.accept("Request Body    : " + readAndMaskJsonContent(request.getHeaders().getContentType(), requestBody, StandardCharsets.UTF_8));
            logger.accept("=================================================Request end(Outbound)=================================================");

            if (statusCode != null) {
                logger.accept("=================================================Response begin(Inbound)=================================================");
                logger.accept("Status code     : " + statusCode.value());
                logger.accept("Headers         : " + formatHeaders(responseHeaders));
                logger.accept("Response Body   : " + readAndMaskJsonContent(responseHeaders.getContentType(), responseBody, StandardCharsets.UTF_8));
                logger.accept("=================================================Response end(Inbound)=================================================");
            }
        } catch (Exception ex) {
            log.error("Exception when log request and response", ex);
        }
    }

    private String formatHeaders(HttpHeaders headers) {
        HttpHeaders clearHttpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            boolean isSensitiveHeader = sensitiveDataHelper.isSensitiveHeader(key);
            clearHttpHeaders.put(key, isSensitiveHeader ? List.of("******") : value);
        });
        return clearHttpHeaders.toString();
    }

    private String readAndMaskJsonContent(MediaType mediaType, byte[] contentBytes, Charset charset) {
        try {
            String contentString = new String(contentBytes, charset);
            if (StringUtils.isEmpty(contentString) || !MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                return contentString;
            }
            return sensitiveDataHelper.maskSensitiveDataFromJson(contentString);
        } catch (Exception ex) {
            log.error("Exception when read content", ex);
            return "Content read error";
        }
    }
}
