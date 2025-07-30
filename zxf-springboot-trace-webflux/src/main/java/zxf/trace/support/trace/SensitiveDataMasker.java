package zxf.trace.support.trace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 敏感数据掩码处理器
 * 用于处理JSON中的敏感字段，将其替换为掩码
 */
@Slf4j
@Component
public class SensitiveDataMasker {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 敏感字段列表，可通过配置文件配置
     */
    private final Set<String> sensitiveFields;

    /**
     * 掩码字符，默认为"******"
     */
    private final String maskValue;

    /**
     * 构造函数，从配置中读取敏感字段列表和掩码字符
     *
     * @param sensitiveFields 敏感字段列表，逗号分隔
     * @param maskValue 掩码字符
     */
    public SensitiveDataMasker(
            @Value("${trace.sensitive-fields:password,token,credit_card,ssn}") String sensitiveFields,
            @Value("${trace.mask-value:******}") String maskValue) {
        this.sensitiveFields = new HashSet<>(Arrays.asList(sensitiveFields.split(",")));
        this.maskValue = maskValue;
        log.info("Sensitive fields configured: {}", this.sensitiveFields);
    }

    /**
     * 对敏感数据进行掩码处理
     *
     * @param data 需要处理的数据
     * @return 处理后的数据
     */
    public String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            // 尝试解析为JSON
            JsonNode jsonNode = objectMapper.readTree(data);
            JsonNode maskedNode = maskJsonNode(jsonNode);
            return objectMapper.writeValueAsString(maskedNode);
        } catch (Exception e) {
            // 如果不是有效的JSON，直接返回原始数据
            log.debug("Failed to parse as JSON, returning original data: {}", e.getMessage());
            return data;
        }
    }

    /**
     * 递归处理JSON节点
     *
     * @param node JSON节点
     * @return 处理后的JSON节点
     */
    private JsonNode maskJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = objectNode.get(fieldName);

                if (sensitiveFields.contains(fieldName.toLowerCase())) {
                    // 如果是敏感字段，替换为掩码
                    objectNode.put(fieldName, maskValue);
                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                    // 如果是对象或数组，递归处理
                    objectNode.set(fieldName, maskJsonNode(fieldValue));
                }
            }

            return objectNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, maskJsonNode(arrayNode.get(i)));
            }
            return arrayNode;
        } else {
            // 基本类型节点，直接返回
            return node;
        }
    }
}
