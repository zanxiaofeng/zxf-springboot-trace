package zxf.trace.support.trace;

import dev.blaauwendraad.masker.json.JsonMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class SensitiveDataHelper {

    public String maskSensitiveDataFromJson(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // block-mode, default masking config
        var jsonMasker = JsonMasker.getMasker(Set.of("email", "token"));

        return jsonMasker.mask(content);
    }

    public Boolean isSensitiveHeader(String headerName) {
        return headerName.equalsIgnoreCase("Token") || headerName.equalsIgnoreCase("Authorization");
    }
}
