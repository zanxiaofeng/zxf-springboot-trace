package zxf.trace.support.trace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {
    private final SensitiveDataHelper sensitiveDataHelper;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            ClientHttpResponse response = execution.execute(request, body);
            logRequestAndResponse(request, body, response);
            return response;
        } catch (Exception ex) {
            log.error("Exception when send request", ex);
            logRequestAndResponse(request, body, null);
            throw ex;
        }
    }

    private void logRequestAndResponse(HttpRequest request, byte[] body, ClientHttpResponse response) throws IOException {
        Consumer<String> logger = response == null || response.getStatusCode().isError() ? log::error : log::info;

        logger.accept("=================================================Request begin(Outbound)=================================================");
        logger.accept("URI             : " + request.getURI());
        logger.accept("Methed          : " + request.getMethod());
        logger.accept("Headers         : " + formatHeaders(request.getHeaders()));
        logger.accept("Request Body    : " + readAndMaskJsonContent(request.getHeaders().getContentType(), body, StandardCharsets.UTF_8));
        logger.accept("=================================================Request end(Outbound)=================================================");

        if (response != null) {
            logger.accept("=================================================Response begin(Inbound)=================================================");
            logger.accept("Status code     : " + response.getStatusCode().value());
            logger.accept("Headers         : " + formatHeaders(response.getHeaders()));
            logger.accept("Response Body   : " + readAndMaskJsonContent(response.getHeaders().getContentType(), StreamUtils.copyToByteArray(response.getBody()), StandardCharsets.UTF_8));
            logger.accept("=================================================Response end(Inbound)=================================================");
        }
    }

    private String formatHeaders(HttpHeaders headers) {
        HttpHeaders clearHttpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            clearHttpHeaders.put(key, sensitiveDataHelper.isSensitiveHeader(key) ? List.of("******") : value);
        });
        return clearHttpHeaders.toString();
    }

    private String readAndMaskJsonContent(MediaType mediaType, byte[] contentBytes, Charset charset) {
        try {
            String contentString = new String(contentBytes, charset);
            if (contentString.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                return contentString;
            }
            return sensitiveDataHelper.maskSensitiveDataFromJson(contentString);
        } catch (Exception ex) {
            log.error("Exception when read content", ex);
            return "Content read error";
        }
    }
}
