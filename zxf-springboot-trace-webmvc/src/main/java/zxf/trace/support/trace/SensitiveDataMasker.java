package zxf.trace.support.trace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class SensitiveDataMasker {

    private final ObjectMapper objectMapper;
    private final Set<String> sensitiveFields;

    public SensitiveDataMasker(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.sensitiveFields = new HashSet<>();
        // 默认敏感字段，可以通过配置文件进行配置
        this.sensitiveFields.add("password");
        this.sensitiveFields.add("token");
        this.sensitiveFields.add("secret");
        this.sensitiveFields.add("creditCard");
        this.sensitiveFields.add("ssn");
    }

    /**
     * 添加敏感字段
     *
     * @param field 敏感字段名称
     */
    public void addSensitiveField(String field) {
        this.sensitiveFields.add(field);
    }

    /**
     * 添加多个敏感字段
     *
     * @param fields 敏感字段名称集合
     */
    public void addSensitiveFields(Set<String> fields) {
        this.sensitiveFields.addAll(fields);
    }

    /**
     * 处理可能包含敏感数据的字符串
     *
     * @param content 可能包含敏感数据的字符串
     * @return 处理后的字符串
     */
    public String maskSensitiveData(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            // 尝试解析为JSON
            JsonNode jsonNode = objectMapper.readTree(content);
            JsonNode maskedNode = maskSensitiveData(jsonNode);
            return objectMapper.writeValueAsString(maskedNode);
        } catch (Exception e) {
            // 如果不是有效的JSON，则直接返回原内容
            log.debug("Content is not valid JSON, returning as is");
            return content;
        }
    }

    /**
     * 处理JsonNode中的敏感数据
     *
     * @param jsonNode JSON节点
     * @return 处理后的JSON节点
     */
    private JsonNode maskSensitiveData(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode value = entry.getValue();

                if (sensitiveFields.contains(fieldName.toLowerCase())) {
                    // 敏感字段，替换为掩码
                    objectNode.put(fieldName, "******");
                } else if (value.isObject() || value.isArray()) {
                    // 递归处理嵌套的对象或数组
                    objectNode.set(fieldName, maskSensitiveData(value));
                }
            }
            return objectNode;
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, maskSensitiveData(arrayNode.get(i)));
            }
            return arrayNode;
        } else {
            return jsonNode;
        }
    }
}
