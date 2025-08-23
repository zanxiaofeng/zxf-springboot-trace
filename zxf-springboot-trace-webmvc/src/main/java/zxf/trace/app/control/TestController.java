package zxf.trace.app.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {
    private final RestTemplate restTemplate;

    @GetMapping("/ok-test")
    public ResponseEntity<Map<String, Object>> okTest(@RequestParam String task) {
        log.info("Processing ok test request for task: {}", task);

        Map<String, Object> response = new HashMap<>();
        response.put("task", task);
        response.put("data", Map.of("id", 1, "name", "test", "token", "this is a sensitive data"));
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok()
                .header("Task", task)
                .header("Authorization", "this is a sensitive data")
                .body(response);
    }

    @GetMapping("/error-test")
    public ResponseEntity<Map<String, Object>> errorTest() {
        log.info("Processing error test request");

        Map<String, Object> response = new HashMap<>();
        response.put("error", "This is a test error");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/exception-test")
    public ResponseEntity<Map<String, Object>> exceptionTest() {
        log.info("Processing exception test request");
        throw new RuntimeException("This is a test exception");
    }

    @PostMapping("/outbound-test")
    public ResponseEntity<Object> outboundTest(@RequestBody Map<String, Object> request) {
        log.info("Processing outbound test request");

        Object response = restTemplate.postForObject("https://jsonplaceholder.typicode.com/posts", request, Object.class);

        return ResponseEntity.ok(response);
    }
}
