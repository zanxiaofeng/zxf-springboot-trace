package zxf.trace.support.trace;

import dev.blaauwendraad.masker.json.JsonMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 敏感数据处理工具类
 * 
 * 提供敏感数据掩码功能，用于在日志记录前处理JSON内容和HTTP头部中的敏感信息，
 * 确保敏感数据不会在日志中明文显示。
 */
@Slf4j
@Component
public class SensitiveDataHelper {
    
    /**
     * 对JSON内容中的敏感数据进行掩码处理
     * 
     * 使用JsonMasker库对JSON字符串中的敏感字段进行掩码处理。
     * 当前配置为掩码"email"和"token"字段。
     * 
     * @param content 需要处理的JSON字符串
     * @return 掩码处理后的JSON字符串
     */
    public String maskSensitiveDataFromJson(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // block-mode, default masking config
        var jsonMasker = JsonMasker.getMasker(Set.of("email", "token"));

        return jsonMasker.mask(content);
    }

    /**
     * 判断HTTP头部名称是否为敏感头部
     * 
     * 检查给定的头部名称是否为预定义的敏感头部（Token或Authorization）。
     * 敏感头部的值将在日志中被掩码处理。
     * 
     * @param headerName HTTP头部名称
     * @return 如果是敏感头部则返回true，否则返回false
     */
    public Boolean isSensitiveHeader(String headerName) {
        return headerName.equalsIgnoreCase("Token") || headerName.equalsIgnoreCase("Authorization");
    }
}
