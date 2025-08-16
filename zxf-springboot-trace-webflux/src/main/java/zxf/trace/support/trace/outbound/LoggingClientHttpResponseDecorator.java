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
    private final ResponseCapture bodyCapture;  // Changed from RequestCapture to ResponseCapture

    // Updated constructor to accept ResponseCapture
    public LoggingClientHttpResponseDecorator(ClientHttpResponse delegate, ResponseCapture bodyCapture) {
        super(delegate);
        this.bodyCapture = bodyCapture;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext(dataBuffer -> {
            try {
                ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
                Channels.newChannel(bodyStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                bodyString = new String(bodyStream.toByteArray());
                if (bodyCapture != null) {
                    bodyCapture.setBodyString(bodyString);  // Capture response body
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    public String getBodyString() {
        return bodyString;
    }
}