package zxf.trace.support.trace;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TraceConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
                //.filter(outboundExchangeFilter);
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
