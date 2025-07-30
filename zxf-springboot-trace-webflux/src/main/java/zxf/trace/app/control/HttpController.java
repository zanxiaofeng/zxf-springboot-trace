package zxf.trace.app.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import zxf.trace.app.service.HttpService;

import java.util.Map;

/**
 * HTTP控制器，用于测试入站和出站请求的日志记录
 */
@Slf4j
@RestController
@RequestMapping("/http")
@RequiredArgsConstructor
public class HttpController {

    private final HttpService httpService;

    /**
     * 处理HTTP调用请求，并从外部服务获取数据
     *
     * @param request 请求体
     * @return 响应数据
     */
    @PostMapping("/calling")
    public Mono<Map<String, Object>> callHttp(@RequestBody Map<String, Object> request) {
        log.info("Received request: {}", request);
        return httpService.callExternalService(request);
    }
}
