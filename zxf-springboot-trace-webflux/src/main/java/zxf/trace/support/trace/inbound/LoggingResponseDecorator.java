package zxf.trace.support.trace.inbound;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
public class LoggingResponseDecorator extends ServerHttpResponseDecorator {
    private String status;
    private String headers;
    private String bodyString;

    public LoggingResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(Flux.from(body).doOnNext(dataBuffer -> {
            try {
                ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
                Channels.newChannel(bodyStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                bodyString = new String(bodyStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public void log(Boolean isError) {
        // 定义日志记录函数，根据响应状态码决定日志级别
        Consumer<String> logger = isError ? log::error : log::info;

        logger.accept("=================================================Response begin(Outbound)=================================================");
        logger.accept(String.format("Status code     : %d", status));
        logger.accept(String.format("Headers         : %s", headers));
        logger.accept(String.format("Response Body   : %s", bodyString));
        logger.accept("=================================================Response end(Outbound)=================================================");
    }
}
