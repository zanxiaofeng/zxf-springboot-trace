package zxf.trace.support.trace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import zxf.trace.OutboundLoggingInterceptor;

@Configuration
public class TraceConfiguration {
    @Autowired
    private OutboundLoggingInterceptor outboundLoggingInterceptor;

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(outboundLoggingInterceptor);
        return restTemplate;
    }

    @Bean("restTemplateWithBuffer")
    public RestTemplate restTemplateWithBuffer() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(requestFactory));
        restTemplate.getInterceptors().add(outboundLoggingInterceptor);
        return restTemplate;
    }
}
