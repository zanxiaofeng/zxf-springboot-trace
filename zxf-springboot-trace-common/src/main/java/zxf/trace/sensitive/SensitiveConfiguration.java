package zxf.trace.sensitive;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "trace.sensitive-mask")
public class SensitiveConfiguration {
    private String[] headers;
    private String[] jsonNames;
}