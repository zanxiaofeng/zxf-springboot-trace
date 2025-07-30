package zxf.trace.support.trace;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TraceConfiguration {

    /**
     * 配置RestTemplate，添加出站日志拦截器
     *
     * @param outboundLoggingInterceptor 出站日志拦截器
     * @return 配置好的RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(OutboundLoggingInterceptor outboundLoggingInterceptor) {
        return new RestTemplateBuilder()
                .interceptors(outboundLoggingInterceptor)
                .build();
    }
}
