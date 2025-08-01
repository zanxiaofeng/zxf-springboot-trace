package zxf.trace.support.trace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * 出站请求日志拦截器
 * <p>
 * 这个拦截器拦截所有通过RestTemplate发出的HTTP请求，
 * 记录请求和响应的详细信息，包括URI、方法、头部和正文内容。
 * 同时处理敏感数据的掩码，确保日志中不会暴露敏感信息。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final SensitiveDataHelper sensitiveDataHelper;

    /**
     * 拦截方法
     * <p>
     * 拦截RestTemplate发出的请求，执行请求，然后记录请求和响应详情。
     * 如果请求执行过程中发生异常，会记录异常信息和请求详情。
     *
     * @param request   HTTP请求
     * @param body      请求体字节数组
     * @param execution 请求执行器
     * @return HTTP响应
     * @throws IOException 如果发生I/O异常
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            ClientHttpResponse response = execution.execute(request, body);
            logRequestAndResponse(request, body, response);
            return response;
        } catch (Exception ex) {
            log.error("Exception when seng request", ex);
            logRequestAndResponse(request, body, null);
            throw ex;
        }
    }

    /**
     * 记录请求和响应日志
     * <p>
     * 根据响应状态码决定日志级别（成功为INFO，错误或无响应为ERROR），
     * 记录请求和响应的详细信息，包括URI、方法、头部和正文内容。
     *
     * @param request  HTTP请求
     * @param body     请求体字节数组
     * @param response HTTP响应，可能为null（如果请求失败）
     * @throws IOException 如果发生I/O异常
     */
    private void logRequestAndResponse(HttpRequest request, byte[] body, ClientHttpResponse response) throws IOException {
        Consumer<String> logger = response == null || response.getStatusCode().isError() ? log::error : log::info;

        logger.accept("=================================================Request begin(Outbound)=================================================");
        logger.accept("URI             : " + request.getURI());
        logger.accept("Methed          : " + request.getMethod());
        logger.accept("Headers         : " + formatHeaders(request.getHeaders()));
        logger.accept("Request Body    : " + readAndMaskContent(body, StandardCharsets.UTF_8));
        logger.accept("=================================================Request end(Outbound)=================================================");

        if (response != null) {
            logger.accept("=================================================Response begin(Inbound)=================================================");
            logger.accept("Status code     : " + response.getStatusCode().value());
            logger.accept("Headers         : " + formatHeaders(response.getHeaders()));
            logger.accept("Response Body   : " + readAndMaskContent(StreamUtils.copyToByteArray(response.getBody()), StandardCharsets.UTF_8));
            logger.accept("=================================================Response end(Inbound)=================================================");
        }
    }

    /**
     * 格式化HTTP头部信息
     * <p>
     * 创建一个新的HttpHeaders对象，将原始头部信息复制过来，
     * 同时掩码敏感头部的值。
     *
     * @param headers 原始HTTP头部
     * @return 格式化后的头部信息字符串
     */
    private String formatHeaders(HttpHeaders headers) {
        HttpHeaders clearHttpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            clearHttpHeaders.put(key, sensitiveDataHelper.isSensitiveHeader(key) ? List.of("******") : value);
        });
        return clearHttpHeaders.toString();
    }

    /**
     * 读取并掩码内容
     * <p>
     * 将字节数组内容转换为字符串，并对JSON内容中的敏感数据进行掩码处理。
     *
     * @param content 内容字节数组
     * @param charset 字符编码
     * @return 掩码处理后的内容字符串
     */
    private String readAndMaskContent(byte[] content, Charset charset) {
        try {
            String contentStr = new String(content, charset);
            return contentStr.isEmpty() ? "" : sensitiveDataHelper.maskSensitiveDataFromJson(contentStr);
        } catch (Exception ex) {
            log.error("Exception when read content", ex);
            return "";
        }
    }
}
