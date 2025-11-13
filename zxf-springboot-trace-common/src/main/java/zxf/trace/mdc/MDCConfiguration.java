package zxf.trace.mdc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "trace.mdc-injection")
public class MDCConfiguration {
    private Boolean enable = false;
    private String key;
    private String header;
}