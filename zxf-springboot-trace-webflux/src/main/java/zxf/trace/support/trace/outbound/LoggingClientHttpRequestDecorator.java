package zxf.trace.support.trace.outbound;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class LoggingClientHttpRequestDecorator extends ClientHttpRequestDecorator {
    private String bodyString;

    public LoggingClientHttpRequestDecorator(ClientHttpRequest delegate) {
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
}
