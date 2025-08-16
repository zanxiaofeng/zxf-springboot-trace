package zxf.trace.support.trace.outbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zxf.trace.support.trace.SensitiveDataHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundExchangeFilter implements ExchangeFilterFunction {
    
    private final SensitiveDataHelper sensitiveDataHelper;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        long startTime = System.currentTimeMillis();

        // Initialize capture objects
        RequestCapture requestCapture = new RequestCapture();
        ResponseCapture responseCapture = new ResponseCapture();

        // Populate basic request metadata
        requestCapture.setPath(request.url().toString());
        requestCapture.setMethod(request.method().name());
        requestCapture.setHeaders(formatHeaders(request.headers()));

        // Decorate request to capture body
        ClientRequest newRequest = ClientRequest.from(request)
            .body((outputMessage, context) ->
                request.body().insert(new LoggingClientHttpRequestDecorator(outputMessage, requestCapture), context)
            ).build();

        return next.exchange(newRequest)
            .flatMap(response -> {
                // Populate basic response metadata
                responseCapture.setStatus(String.valueOf(response.statusCode().value()));
                responseCapture.setHeaders(formatHeaders(response.headers().asHttpHeaders()));

                // Capture response body
                Flux<DataBuffer> loggedBody = response.bodyToFlux(DataBuffer.class)
                    .doOnNext(dataBuffer -> {
                        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                            Channels.newChannel(outputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                            responseCapture.setBodyString(new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                // Build decorated response
                ClientResponse decoratedResponse = ClientResponse.from(response)
                    .body(loggedBody)
                    .build();

                return Mono.just(decoratedResponse);
            })
            .doFinally(
                    signalType -> logRequestAndResponse(requestCapture, responseCapture, System.currentTimeMillis() - startTime, null));
    }
    
    // Updated logging method to use capture objects
    private void logRequestAndResponse(RequestCapture requestCapture,
                                     ResponseCapture responseCapture,
                                     long duration, Throwable throwable) {
        try {
            Consumer<String> logger = (responseCapture == null ||
                                      (responseCapture.getStatus() != null &&
                                       Integer.parseInt(responseCapture.getStatus()) >= 400)) ? log::error : log::info;

            // Log request details from RequestCapture
            logger.accept("=================================================Request begin(Outbound)=================================================");
            logger.accept("URI             : " + requestCapture.getPath());
            logger.accept("Method          : " + requestCapture.getMethod());
            logger.accept("Headers         : " + requestCapture.getHeaders());
            logger.accept("Request Body    : " + readAndMaskContent(requestCapture.getBodyString(), StandardCharsets.UTF_8));
            logger.accept("=================================================Request end(Outbound)=================================================");

            // Log response details from ResponseCapture
            if (responseCapture != null) {
                logger.accept("=================================================Response begin(Inbound)=================================================");
                logger.accept("Status code     : " + responseCapture.getStatus());
                logger.accept("Headers         : " + responseCapture.getHeaders());
                logger.accept("Response Body   : " + readAndMaskContent(responseCapture.getBodyString(), StandardCharsets.UTF_8));
                logger.accept("Duration        : " + duration + " ms");
                logger.accept("=================================================Response end(Inbound)=================================================");
            }
            
            // Log error details
            if (throwable != null) {
                logger.accept("=================================================Error begin=================================================");
                logger.accept("Error           : " + throwable.getMessage());
                logger.accept("=================================================Error end=================================================");
            }
        } catch (Exception ex) {
            log.error("Exception when log request and response", ex);
        }
    }

    private String formatHeaders(HttpHeaders headers) {
        HttpHeaders clearHttpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            clearHttpHeaders.put(key, sensitiveDataHelper.isSensitiveHeader(key) ? List.of("******") : value);
        });
        return clearHttpHeaders.toString();
    }

    private String readAndMaskContent(String content, java.nio.charset.Charset charset) {
        try {
            return content == null || content.isEmpty() ? "" : sensitiveDataHelper.maskSensitiveDataFromJson(content);
        } catch (Exception ex) {
            log.error("Exception when read content", ex);
            return "";
        }
    }
}