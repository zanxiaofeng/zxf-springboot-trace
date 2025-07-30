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
        // 记录请求日志
        logRequest(request, body);

        // 执行请求
        ClientHttpResponse response = execution.execute(request, body);

        // 记录响应日志
        logResponse(request, response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        // 在请求阶段，我们还不知道响应状态码，所以先使用DEBUG级别
        if (log.isDebugEnabled()) {
            log.debug("=================================================Request begin(Outbound)=================================================");
            log.debug("URI             : {}", request.getURI());
            log.debug("Methed          : {}", request.getMethod());
            log.debug("Headers         : {}", request.getHeaders().entrySet().stream()
                    .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                    .toList());

            // 获取请求体
            String requestBody = new String(body, StandardCharsets.UTF_8);
            if (!requestBody.isEmpty()) {
                // 处理敏感数据
                requestBody = sensitiveDataMasker.maskSensitiveData(requestBody);
            }
            log.debug("Request Body    : {}", requestBody);
            log.debug("=================================================Request end(Outbound)=================================================");
        }
    }

    private void logResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        // 获取响应状态码
        int statusCode = response.getStatusCode().value();
        boolean isError = statusCode != 200;

        // 读取响应体
        byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
        String responseBodyStr = new String(responseBody, StandardCharsets.UTF_8);
        if (!responseBodyStr.isEmpty()) {
            // 处理敏感数据
            responseBodyStr = sensitiveDataMasker.maskSensitiveData(responseBodyStr);
        }

        // 如果是错误状态码，使用ERROR级别，否则使用DEBUG级别
        if (isError) {
            // 如果之前使用DEBUG级别记录了请求，现在需要用ERROR级别重新记录
            log.error("=================================================Request begin(Outbound)=================================================");
            log.error("URI             : {}", request.getURI());
            log.error("Methed          : {}", request.getMethod());
            log.error("Headers         : {}", request.getHeaders().entrySet().stream()
                    .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                    .toList());

            // 由于body在这个方法中不可用，我们不能记录请求体
            log.error("Request Body    : [Not available in response phase]");
            log.error("=================================================Request end(Outbound)=================================================");

            // 记录响应
            log.error("=================================================Response begin(Outbound)=================================================");
            log.error("Status code     : {}", statusCode);
            log.error("Status text     : {}", response.getStatusText());
            log.error("Headers         : {}", response.getHeaders().entrySet().stream()
                    .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                    .toList());
            log.error("Response Body   : {}", responseBodyStr);
            log.error("=================================================Response end(Outbound)=================================================");
        } else if (log.isDebugEnabled()) {
            // 使用DEBUG级别记录响应
            log.debug("=================================================Response begin(Outbound)=================================================");
            log.debug("Status code     : {}", statusCode);
            log.debug("Status text     : {}", response.getStatusText());
            log.debug("Headers         : {}", response.getHeaders().entrySet().stream()
                    .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
                    .toList());
            log.debug("Response Body   : {}", responseBodyStr);
            log.debug("=================================================Response end(Outbound)=================================================");
        }
    }
}
