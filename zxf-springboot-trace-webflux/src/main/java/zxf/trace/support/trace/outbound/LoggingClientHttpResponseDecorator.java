package zxf.trace.support.trace.outbound;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ClientHttpResponseDecorator;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class LoggingClientHttpResponseDecorator extends ClientHttpResponseDecorator {
    private String bodyString;

    public LoggingClientHttpResponseDecorator(ClientHttpResponse delegate) {
        super(delegate);
    }

    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext(dataBuffer -> {
            try {
                ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
                Channels.newChannel(bodyStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                bodyString = new String(bodyStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
