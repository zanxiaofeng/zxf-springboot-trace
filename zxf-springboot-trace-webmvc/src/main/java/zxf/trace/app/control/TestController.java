package zxf.trace.app.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 
 * 提供用于测试入站和出站请求日志记录功能的API端点。
 * 包含成功响应测试、错误响应测试和出站请求测试三个端点。
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final RestTemplate restTemplate;

    /**
     * 成功响应测试端点
     * 
     * 返回200 OK响应，包含请求参数和一些测试数据。
     * 响应中包含敏感数据，用于测试敏感数据掩码功能。
     * 
     * @param task 任务名称参数
     * @return 包含测试数据的成功响应
     */
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

    /**
     * 错误响应测试端点
     * 
     * 返回400 Bad Request响应，包含错误信息。
     * 用于测试错误响应的日志记录功能。
     * 
     * @return 包含错误信息的400响应
     */
    @GetMapping("/error-test")
    public ResponseEntity<Map<String, Object>> errorTest() {
        log.info("Processing error test request");

        Map<String, Object> response = new HashMap<>();
        response.put("error", "This is a test error");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 出站请求测试端点
     * 
     * 接收请求体并使用RestTemplate向外部API发送POST请求。
     * 用于测试出站请求的日志记录功能。
     * 
     * @param request 请求体，将被转发到外部API
     * @return 外部API的响应
     */
    @PostMapping("/outbound-test")
    public ResponseEntity<Object> outboundTest(@RequestBody Map<String, Object> request) {
        log.info("Processing outbound test request");

        // 使用RestTemplate发起出站请求
        ResponseEntity<Object> response = restTemplate.postForEntity("https://jsonplaceholder.typicode.com/posts", request, Object.class);

        return ResponseEntity.ok(response.getBody());
    }
}
