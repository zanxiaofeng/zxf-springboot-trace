package zxf.trace.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * HTTP服务，用于调用外部服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpService {

    private final WebClient webClient;

    /**
     * 调用外部服务
     *
     * @param request 请求参数
     * @return 响应数据
     */
    public Mono<Map<String, Object>> callExternalService(Map<String, Object> request) {
        log.debug("Calling external service with request: {}", request);

        return webClient.post()
                .uri("http://localhost:8089/pa/a/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.debug("Received response from external service: {}", response))
                .doOnError(error -> log.error("Error calling external service", error));
    }
}
