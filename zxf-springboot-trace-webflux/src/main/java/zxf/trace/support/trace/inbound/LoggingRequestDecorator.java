package zxf.trace.support.trace.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.function.Consumer;

@Slf4j
public class LoggingRequestDecorator extends ServerHttpRequestDecorator {
    private String path;
    private String method;
    private String headers;
    private String bodyString;

    public LoggingRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
        path = delegate.getPath().value();
        method = delegate.getMethod().toString();
        headers = delegate.getHeaders().toString();
        bodyString = "";
    }

    @Override
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

    public void log(Boolean isError) {
        Consumer<String> logger = isError ? log::error : log::info;

        logger.accept("=================================================Request begin(Inbound)=================================================");
        logger.accept(String.format("URI             : %s", path));
        logger.accept(String.format("Method          : %s", method));
        logger.accept(String.format("Headers         : %s", headers));
        logger.accept(String.format("Request Body    : %s", bodyString));
        logger.accept("=================================================Request end(Inbound)=================================================");
    }

}
