package zxf.trace.support.trace.outbound;

import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

public class LoggingClientHttpRequestDecorator extends ClientHttpRequestDecorator {
    private String bodyString;
    private final RequestCapture bodyCapture;  // Uses RequestCapture for requests

    public LoggingClientHttpRequestDecorator(ClientHttpRequest delegate, RequestCapture bodyCapture) {
        super(delegate);
        this.bodyCapture = bodyCapture;
    }

    public Mono<Void> writeWith(Flux<DataBuffer> body) {
        return getDelegate().writeWith(Flux.from(body).doOnNext(dataBuffer -> {
            try (ByteArrayOutputStream bodyStream = new ByteArrayOutputStream()) {
                Channels.newChannel(bodyStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                bodyString = new String(bodyStream.toByteArray(), StandardCharsets.UTF_8);
                bodyCapture.setBodyString(bodyString);  // Populates RequestCapture body
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }
    
    public String getBodyString() {
        return bodyString;
    }
}