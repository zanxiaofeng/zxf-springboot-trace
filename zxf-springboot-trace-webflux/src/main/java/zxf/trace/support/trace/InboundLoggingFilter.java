package zxf.trace.support.trace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboundLoggingFilter implements WebFilter {

    private final SensitiveDataMasker sensitiveDataMasker;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 记录请求信息
        if (log.isDebugEnabled()) {
            log.debug("=================================================Request begin(Inbound)=================================================");
            log.debug("URI             : {}", request.getURI());
            log.debug("Method          : {}", request.getMethod());
            log.debug("Headers         : {}", formatHeaders(request.getHeaders()));

            // 读取请求体
            return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String requestBody = new String(bytes, StandardCharsets.UTF_8);
                    if (!requestBody.isEmpty()) {
                        requestBody = sensitiveDataMasker.maskSensitiveData(requestBody);
                    }
                    log.debug("Request Body    : {}", requestBody);
                    log.debug("=================================================Request end(Inbound)=================================================");

                    // 重新创建请求体
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                        }
                    };

                    // 装饰响应以记录响应信息
                    ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(response) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                                return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                                    // 合并所有数据缓冲区
                                    int length = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
                                    byte[] content = new byte[length];
                                    int offset = 0;
                                    for (DataBuffer dataBuffer : dataBuffers) {
                                        int count = dataBuffer.readableByteCount();
                                        dataBuffer.read(content, offset, count);
                                        offset += count;
                                        DataBufferUtils.release(dataBuffer);
                                    }

                                    // 记录响应信息
                                    HttpStatus status = getStatusCode();
                                    boolean isError = status != null && status.isError();

                                    String responseBody = new String(content, StandardCharsets.UTF_8);
                                    if (!responseBody.isEmpty()) {
                                        responseBody = sensitiveDataMasker.maskSensitiveData(responseBody);
                                    }

                                    if (isError) {
                                        // 如果是错误状态码，使用ERROR级别
                                        log.error("=================================================Response begin(Inbound)=================================================");
                                        log.error("Status code     : {}", status.value());
                                        log.error("Status text     : {}", status.getReasonPhrase());
                                        log.error("Headers         : {}", formatHeaders(getHeaders()));
                                        log.error("Response Body   : {}", responseBody);
                                        log.error("=================================================Response end(Inbound)=================================================");
                                    } else if (log.isDebugEnabled()) {
                                        // 使用DEBUG级别记录响应
                                        log.debug("=================================================Response begin(Inbound)=================================================");
                                        log.debug("Status code     : {}", status != null ? status.value() : "Unknown");
                                        log.debug("Status text     : {}", status != null ? status.getReasonPhrase() : "Unknown");
                                        log.debug("Headers         : {}", formatHeaders(getHeaders()));
                                        log.debug("Response Body   : {}", responseBody);
                                        log.debug("=================================================Response end(Inbound)=================================================");
                                    }

                                    // 重新创建响应体
                                    return exchange.getResponse().bufferFactory().wrap(content);
                                }).flatMap(dataBuffer -> super.writeWith(Mono.just(dataBuffer))));
                            }
                            return super.writeWith(body);
                        }
                    };

                    // 继续过滤链
                    return chain.filter(exchange.mutate().request(mutatedRequest).response(responseDecorator).build());
                })
                .switchIfEmpty(chain.filter(exchange));
        }

        // 如果不是DEBUG级别，直接继续过滤链
        return chain.filter(exchange);
    }

    /**
     * 格式化HTTP头
     *
     * @param headers HTTP头
     * @return 格式化后的HTTP头
     */
    private List<String> formatHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                .toList();
    }
}
