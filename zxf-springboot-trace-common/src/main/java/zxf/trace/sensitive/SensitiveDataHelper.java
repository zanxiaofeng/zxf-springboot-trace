package zxf.trace.sensitive;

import dev.blaauwendraad.masker.json.JsonMasker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SensitiveDataHelper {
    @Autowired
    private SensitiveConfiguration sensitiveConfiguration;

    public String maskSensitiveDataFromJson(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // block-mode, default masking config
        var jsonMasker = JsonMasker.getMasker(Set.of(sensitiveConfiguration.getJsonNames()));

        return jsonMasker.mask(content);
    }

    public Boolean isSensitiveHeader(String headerName) {
        if (headerName == null || headerName.isEmpty()) {
            return false;
        }

        for (String sensitiveHeader : sensitiveConfiguration.getHeaders()) {
            if (headerName.equalsIgnoreCase(sensitiveHeader)) {
                return true;
            }
        }

        return false;
    }
}
