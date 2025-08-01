//package zxf.trace.support.trace.outbound;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.ClientRequest;
//import org.springframework.web.reactive.function.client.ClientResponse;
//import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
//import org.springframework.web.reactive.function.client.ExchangeFunction;
//import reactor.core.publisher.Mono;
//import zxf.trace.support.trace.SensitiveDataHelper;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OutboundExchangeFilter implements ExchangeFilterFunction {
//
//    /**
//     * 获取日志记录器
//     * @return 日志记录器
//     */
//    public org.slf4j.Logger getLog() {
//        return log;
//    }
//
//    private final SensitiveDataHelper sensitiveDataMasker;
//
//    @Override
//    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
//        // 保存请求信息，以便在响应阶段使用
//        final String requestUri = request.url().toString();
//        final String requestMethod = request.method().toString();
//        final List<String> requestHeaders = formatHeaders(request.headers());
//
//        // 继续请求，并记录响应
//        return next.exchange(request)
//                .flatMap(response -> {
//                    // 获取响应状态
//                    HttpStatus status = response.statusCode();
//                    boolean isError = status.isError();
//
//                    // 创建一个新的响应，以便我们可以读取响应体
//                    return response.bodyToMono(String.class)
//                            .defaultIfEmpty("")
//                            .flatMap(body -> {
//                                // 处理敏感数据
//                                String processedBody = sensitiveDataMasker.maskSensitiveData(body);
//
//                                // 根据响应状态码决定日志级别，确保请求和响应使用相同的日志级别
//                                if (isError) {
//                                    // 使用ERROR级别记录请求
//                                    log.error("=================================================Request begin(Outbound)=================================================");
//                                    log.error("URI             : {}", requestUri);
//                                    log.error("Method          : {}", requestMethod);
//                                    log.error("Headers         : {}", requestHeaders);
//                                    log.error("Request Body    : [Body content not available for logging in filter]");
//                                    log.error("=================================================Request end(Outbound)=================================================");
//
//                                    // 使用ERROR级别记录响应
//                                    log.error("=================================================Response begin(Outbound)=================================================");
//                                    log.error("Status code     : {}", status.value());
//                                    log.error("Status text     : {}", status.getReasonPhrase());
//                                    log.error("Headers         : {}", formatHeaders(response.headers().asHttpHeaders()));
//                                    log.error("Response Body   : {}", processedBody);
//                                    log.error("=================================================Response end(Outbound)=================================================");
//                                } else if (log.isDebugEnabled()) {
//                                    // 使用DEBUG级别记录请求
//                                    log.debug("=================================================Request begin(Outbound)=================================================");
//                                    log.debug("URI             : {}", requestUri);
//                                    log.debug("Method          : {}", requestMethod);
//                                    log.debug("Headers         : {}", requestHeaders);
//                                    log.debug("Request Body    : [Body content not available for logging in filter]");
//                                    log.debug("=================================================Request end(Outbound)=================================================");
//
//                                    // 使用DEBUG级别记录响应
//                                    log.debug("=================================================Response begin(Outbound)=================================================");
//                                    log.debug("Status code     : {}", status.value());
//                                    log.debug("Status text     : {}", status.getReasonPhrase());
//                                    log.debug("Headers         : {}", formatHeaders(response.headers().asHttpHeaders()));
//                                    log.debug("Response Body   : {}", processedBody);
//                                    log.debug("=================================================Response end(Outbound)=================================================");
//                                }
//
//                                // 重新创建响应
//                                ClientResponse newResponse = ClientResponse.create(status)
//                                        .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
//                                        .body(body.getBytes(StandardCharsets.UTF_8))
//                                        .build();
//
//                                return Mono.just(newResponse);
//                            });
//                });
//    }
//
//    /**
//     * 格式化HTTP头
//     *
//     * @param headers HTTP头
//     * @return 格式化后的HTTP头
//     */
//    private List<String> formatHeaders(org.springframework.http.HttpHeaders headers) {
//        return headers.entrySet().stream()
//                .map(entry -> String.format("%s:\"%s\"", entry.getKey(), entry.getValue()))
//                .collect(Collectors.toList());
//    }
//}
