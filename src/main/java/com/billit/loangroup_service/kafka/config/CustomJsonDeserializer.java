package com.billit.loangroup_service.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class CustomJsonDeserializer<T> implements Deserializer<T> {
    private final ObjectMapper objectMapper;
    private Class<T> targetType;

    public CustomJsonDeserializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (configs.containsKey("value.deserializer.type")) {
            this.targetType = (Class<T>) configs.get("value.deserializer.type");
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            if (targetType == null) {
                throw new SerializationException("Target type is not configured for deserialization");
            }
            return objectMapper.readValue(data, targetType);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing value", e);
        }
    }

    @Override
    public void close() {}
}
