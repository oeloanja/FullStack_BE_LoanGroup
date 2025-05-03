package com.billit.loangroup_service.kafka.config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.header.Headers;


public class CustomJsonSerializer<T> implements Serializer<T> {
    private final ObjectMapper objectMapper;

    public CustomJsonSerializer() {
        // ObjectMapper에 JavaTimeModule 등록
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            // 데이터를 JSON으로 직렬화
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error serializing value", e);
        }
    }
}