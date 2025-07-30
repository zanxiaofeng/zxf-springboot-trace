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

    /**
     * 测试入站请求日志
     *
     * @param name 名称参数
     * @return 响应
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello(@RequestParam(defaultValue = "World") String name) {
        log.info("Processing hello request for name: {}", name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试入站请求日志（POST方法）
     *
     * @param request 请求体
     * @return 响应
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> request) {
        log.info("Processing echo request: {}", request);
        
        Map<String, Object> response = new HashMap<>(request);
        response.put("timestamp", System.currentTimeMillis());
        response.put("echoed", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试出站请求日志
     *
     * @return 响应
     */
    @GetMapping("/outbound-test")
    public ResponseEntity<Object> outboundTest() {
        log.info("Processing outbound test request");
        
        // 使用RestTemplate发起出站请求
        ResponseEntity<Object> response = restTemplate.getForEntity(
                "https://jsonplaceholder.typicode.com/posts/1", Object.class);
        
        return ResponseEntity.ok(response.getBody());
    }

    /**
     * 测试错误响应
     *
     * @return 错误响应
     */
    @GetMapping("/error-test")
    public ResponseEntity<Map<String, Object>> errorTest() {
        log.info("Processing error test request");

        Map<String, Object> response = new HashMap<>();
        response.put("error", "This is a test error");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.badRequest().body(response);
    }
}
