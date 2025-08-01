package zxf.trace.support.rest;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebFlux错误处理配置
 *
 * 提供WebFlux错误处理所需的配置Bean。
 * 在Spring Boot 2.4.0及以上版本中，WebProperties.Resources不再自动注册为Bean，
 * 因此需要手动注册以供GlobalExceptionHandler使用。
 */
@Configuration
public class WebFluxErrorConfig {

    /**
     * 创建WebProperties.Resources Bean
     *
     * @return WebProperties.Resources实例
     */
    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }
}
