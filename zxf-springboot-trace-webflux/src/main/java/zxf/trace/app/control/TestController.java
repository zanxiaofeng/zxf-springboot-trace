package zxf.trace.app.control;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final WebClient webClient;

    @GetMapping("/ok-test")
    public Mono<ResponseEntity> okTest(@RequestParam String task) {
        log.info("Processing ok test request for task: {}", task);

        Map<String, Object> response = new HashMap<>();
        response.put("task", task);
        response.put("data", Map.of("id", 1, "name", "test", "token", "this is a sensitive data"));
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.ok()
                .header("Task", task)
                .header("Authorization", "this is a sensitive data")
                .body(response));
    }


    @GetMapping("/error-test")
    public Mono<ResponseEntity<Map<String, Object>>> errorTest() {
        log.info("Processing error test request");

        Map<String, Object> response = new HashMap<>();
        response.put("error", "This is a test error");
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.badRequest().body(response));
    }


    @PostMapping("/outbound-test")
    public Mono<ResponseEntity<Object>> outboundTest(@RequestBody Map<String, Object> request) {
        log.info("Processing outbound test request");

        Object response = webClient.post()
                .uri("https://jsonplaceholder.typicode.com/posts")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class);

        return Mono.just(ResponseEntity.ok(response));
    }
}
