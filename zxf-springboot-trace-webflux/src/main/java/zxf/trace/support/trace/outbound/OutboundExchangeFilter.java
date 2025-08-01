package zxf.trace.support.trace.outbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import zxf.trace.support.trace.SensitiveDataHelper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundExchangeFilter implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest newRequest = ClientRequest.from(request)
                .body((outputMessage, context) -> {
                    LoggingClientHttpRequestDecorator loggingClientHttpRequestDecorator = new LoggingClientHttpRequestDecorator(outputMessage);
                    return request.body().insert(loggingClientHttpRequestDecorator, context);
                }).build();

        next.exchange(newRequest).map(response -> {
            ClientResponse.create(response).body((inputMessage, context) -> {
                LoggingClientHttpResponseDecorator loggingClientHttpResponseDecorator = new LoggingClientHttpResponseDecorator(inputMessage);
                return response.body().map(loggingClientHttpResponseDecorator);
            }).build();






}
