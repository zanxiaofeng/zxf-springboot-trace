package zxf.trace.support.trace;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TraceConfiguration {

    /**
     * 配置WebClient，添加出站日志过滤器
     *
     * @param outboundExchangeFilter 出站日志过滤器
     * @return 配置好的WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder(OutboundExchangeFilter outboundExchangeFilter) {
        return WebClient.builder()
                .filter(outboundExchangeFilter);
    }

    /**
     * 配置默认WebClient实例
     *
     * @param webClientBuilder WebClient.Builder
     * @return 配置好的WebClient
     */
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
