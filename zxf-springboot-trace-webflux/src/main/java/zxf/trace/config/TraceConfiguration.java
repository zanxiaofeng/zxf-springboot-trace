package zxf.trace.config;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zxf.trace.support.trace.OutboundExchangeFilter;
import zxf.trace.support.trace.SensitiveDataMasker;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 跟踪配置类，用于配置WebClient和过滤器
 */
@Configuration
public class TraceConfiguration {

    /**
     * 配置WebClient，添加请求和响应的日志记录
     */
    @Bean
    public WebClient webClient(OutboundExchangeFilter outboundExchangeFilter, SensitiveDataMasker sensitiveDataMasker) {
        // 创建能够记录请求体的WebClient
        return WebClient.builder()
                .filter((request, next) -> {
                    // 创建一个原子引用来存储请求体
                    AtomicReference<String> requestBodyRef = new AtomicReference<>("");

                    // 创建一个新的ClientRequest，它会捕获请求体
                    ClientRequest newRequest = ClientRequest.from(request)
                            .body((outputMessage, context) -> {
                                ClientHttpRequestDecorator decorator = new ClientHttpRequestDecorator(outputMessage) {
                                    @Override
                                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                        return Flux.from(body)
                                                .collectList()
                                                .flatMap(dataBuffers -> {
                                                    // 合并所有DataBuffer
                                                    DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
                                                    DataBuffer joinedBuffer = bufferFactory.join(dataBuffers);
                                                    byte[] content = new byte[joinedBuffer.readableByteCount()];
                                                    joinedBuffer.read(content);

                                                    // 存储请求体
                                                    String bodyStr = new String(content, StandardCharsets.UTF_8);
                                                    requestBodyRef.set(sensitiveDataMasker.maskSensitiveData(bodyStr));

                                                    // 写入原始请求体
                                                    return super.writeWith(Mono.just(bufferFactory.wrap(content)));
                                                });
                                    }
                                };
                                return request.body().insert(decorator, context);
                            }).build();

                    // 使用修改后的请求继续过滤器链
                    return next.exchange(newRequest)
                            .doOnNext(response -> {
                                // 在这里可以访问捕获的请求体
                                if (!requestBodyRef.get().isEmpty() && outboundExchangeFilter.getLog().isDebugEnabled()) {
                                    outboundExchangeFilter.getLog().debug("Request Body    : {}", requestBodyRef.get());
                                }
                            });
                })
                .filter(outboundExchangeFilter)
                .build();
    }
}
