package zxf.trace.mdc;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MDCHelper {
    @Autowired
    private MDCConfiguration mdcConfiguration;

    public void inject(HttpServletRequest request) {
        if (mdcConfiguration.getEnable()) {
            MDC.put(mdcConfiguration.getKey(), request.getHeader(mdcConfiguration.getHeader()));
        }
    }

    public void clean() {
        if (mdcConfiguration.getEnable()) {
            MDC.remove(mdcConfiguration.getKey());
        }
    }
}
