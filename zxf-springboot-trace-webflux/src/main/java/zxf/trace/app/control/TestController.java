packagetrace.app.control;

import lombok.RequiredArgsConstruct zxf.or;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final WebClient webClient;

    /**
     * 测试入站请求日志
     *
     * @param name 名称参数
     * @return 响应
     */
    @GetMapping("/hello")
    public Mono<Map<String, Object>> hello(@RequestParam(defaultValue = "World") String name) {
        log.info("Processing hello request for name: {}", name);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(response);
    }

    /**
     * 测试入站请求日志（POST方法）
     *
     * @param request 请求体
     * @return 响应
     */
    @PostMapping("/echo")
    public Mono<Map<String, Object>> echo(@RequestBody Map<String, Object> request) {
        log.info("Processing echo request: {}", request);

        Map<String, Object> response = new HashMap<>(request);
        response.put("timestamp", System.currentTimeMillis());
        response.put("echoed", true);

        return Mono.just(response);
    }

    /**
     * 业务需求中指定的端点，从外部服务获取数据
     *
     * @param request 请求体
     * @return 从外部服务获取的数据
     */
    @PostMapping("/http/calling")
    public Mono<Object> httpCalling(@RequestBody Map<String, Object> request) {
        log.info("Processing http calling request: {}", request);

        return webClient.post()
                .uri("http://localhost:8089/pa/a/json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class);
    }

    /**
     * 测试出站请求日志
     *
     * @return 响应
     */
    @GetMapping("/outbound-test")
    public Mono<Object> outboundTest() {
        log.info("Processing outbound test request");

        return webClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts/1")
                .retrieve()
                .bodyToMono(Object.class);
    }

    /**
     * 测试错误响应
     *
     * @return 错误响应
     */
    @GetMapping("/error-test")
    public Mono<Map<String, Object>> errorTest() {
        log.info("Processing error test request");

        return Mono.error(new RuntimeException("Test error"));
    }
}
