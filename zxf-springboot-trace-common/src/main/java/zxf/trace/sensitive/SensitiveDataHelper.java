package zxf.trace.sensitive;

import dev.blaauwendraad.masker.json.JsonMasker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SensitiveDataHelper {
    @Autowired
    private SensitiveConfiguration sensitiveConfiguration;

    public String maskSensitiveDataFromJson(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }

        // block-mode, default masking config
        var jsonMasker = JsonMasker.getMasker(Set.of(sensitiveConfiguration.getJsonNames()));

        return jsonMasker.mask(content);
    }

    public Boolean isSensitiveHeader(String headerName) {
        if (StringUtils.isEmpty(headerName)) {
            return false;
        }

        for (String sensitiveHeader : sensitiveConfiguration.getHeaders()) {
            if (StringUtils.equalsIgnoreCase(headerName, sensitiveHeader)) {
                return true;
            }
        }

        return false;
    }
}
