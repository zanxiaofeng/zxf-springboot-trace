package zxf.trace.support.trace.inbound;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.SignalType;

public class LoggingWebExchange extends ServerWebExchangeDecorator {
    private final LoggingRequestDecorator loggingRequestDecorator;
    private final LoggingResponseDecorator loggingResponseDecorator;

    protected LoggingWebExchange(ServerWebExchange delegate) {
        super(delegate);
        this.loggingRequestDecorator = new LoggingRequestDecorator(delegate.getRequest());
        this.loggingResponseDecorator = new LoggingResponseDecorator(delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return loggingRequestDecorator;
    }

    public ServerHttpResponse getResponse() {
        return loggingResponseDecorator;
    }

    public void log(SignalType signalType) {
       Boolean isError = loggingResponseDecorator.getStatusCode().isError();
        loggingRequestDecorator.log(isError);
        loggingResponseDecorator.log(isError);
    }
}
