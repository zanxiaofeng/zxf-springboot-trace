package zxf.trace.support.trace.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import zxf.trace.support.trace.SensitiveDataHelper;


@Slf4j
@Component
@RequiredArgsConstructor
public class InboundLoggingFilter implements WebFilter {

    private final SensitiveDataHelper sensitiveDataMasker;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        LoggingWebExchange loggingWebExchange = new LoggingWebExchange(exchange);
        return chain.filter(loggingWebExchange).doFinally((signalType) -> {
            loggingWebExchange.log(signalType);
        });
    }
}
