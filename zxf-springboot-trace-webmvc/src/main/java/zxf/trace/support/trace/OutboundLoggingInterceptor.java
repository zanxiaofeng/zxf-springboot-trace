package zxf.trace.support.trace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final SensitiveDataMasker sensitiveDataMasker;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 保存请求信息，以便在响应阶段使用
        String requestBody = new String(body, StandardCharsets.UTF_8);
        if (!requestBody.isEmpty()) {
            // 处理敏感数据
            requestBody = sensitiveDataMasker.maskSensitiveData(requestBody);
        }

        // 执行请求
        ClientHttpResponse response = execution.execute(request, body);

        // 记录请求和响应日志
        logRequestAndResponse(request, requestBody, response);

        return response;
    }

    private void logRequestAndResponse(HttpRequest request, String requestBody, ClientHttpResponse response) throws IOException {
        // 获取响应状态码
        int statusCode = response.getStatusCode().value();
        boolean isError = statusCode != 200;

        // 读取响应体
        byte[] responseBodyBytes = StreamUtils.copyToByteArray(response.getBody());
        String responseBodyStr = new String(responseBodyBytes, StandardCharsets.UTF_8);
        if (!responseBodyStr.isEmpty()) {
            // 处理敏感数据
            responseBodyStr = sensitiveDataMasker.maskSensitiveData(responseBodyStr);
        }

        // 准备请求日志内容
        String requestUri = request.getURI().toString();
        String requestMethod = request.getMethod().toString();
        String requestHeaders = request.getHeaders().entrySet().stream()
                .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                .toList().toString();

        // 准备响应日志内容
        String responseHeaders = response.getHeaders().entrySet().stream()
                .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                .toList().toString();

        // 根据响应状态码决定日志级别，确保请求和响应使用相同的日志级别
        if (isError) {
            // 使用ERROR级别记录请求
            log.error("=================================================Request begin(Outbound)=================================================");
            log.error("URI             : {}", requestUri);
            log.error("Methed          : {}", requestMethod);
            log.error("Headers         : {}", requestHeaders);
            log.error("Request Body    : {}", requestBody);
            log.error("=================================================Request end(Outbound)=================================================");

            // 记录响应
            log.error("=================================================Response begin(Outbound)=================================================");
            log.error("Status code     : {}", statusCode);
            log.error("Status text     : {}", response.getStatusText());
            log.error("Headers         : {}", responseHeaders);
            log.error("Response Body   : {}", responseBodyStr);
            log.error("=================================================Response end(Outbound)=================================================");
        } else if (log.isDebugEnabled()) {
            // 使用DEBUG级别记录请求
            log.debug("=================================================Request begin(Outbound)=================================================");
            log.debug("URI             : {}", requestUri);
            log.debug("Methed          : {}", requestMethod);
            log.debug("Headers         : {}", requestHeaders);
            log.debug("Request Body    : {}", requestBody);
            log.debug("=================================================Request end(Outbound)=================================================");

            // 使用DEBUG级别记录响应
            log.debug("=================================================Response begin(Outbound)=================================================");
            log.debug("Status code     : {}", statusCode);
            log.debug("Status text     : {}", response.getStatusText());
            log.debug("Headers         : {}", responseHeaders);
            log.debug("Response Body   : {}", responseBodyStr);
            log.debug("=================================================Response end(Outbound)=================================================");
        }
    }
}
