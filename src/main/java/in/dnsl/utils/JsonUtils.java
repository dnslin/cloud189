package in.dnsl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Slf4j
public class JsonUtils {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 通用的JSON处理方法
    private static <T> T handleJsonOperation(@NotNull JsonOperation<T> operation) {
        try {
            return operation.execute();
        } catch (JsonProcessingException e) {
            log.error("JSON操作失败: {}", e.getMessage());
            return null;
        }
    }

    // JSON转对象
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        return handleJsonOperation(() -> objectMapper.readValue(json, clazz));
    }

    // 对象转JSON
    public static String objectToJson(Object obj) {
        return handleJsonOperation(() -> objectMapper.writeValueAsString(obj));
    }

    // JSON转Map
    public static Map<String, Object> jsonToMap(String json) {
        return handleJsonOperation(() -> objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        }));
    }

    // 对象转Map
    public static Map<String, Object> objectToMap(Object obj) {
        return handleJsonOperation(() -> objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
        }));
    }

    // Map转JSON
    public static String mapToJson(Map<String, Object> map) {
        return handleJsonOperation(() -> objectMapper.writeValueAsString(map));
    }

    @FunctionalInterface
    private interface JsonOperation<T> {
        T execute() throws JsonProcessingException;
    }
}

