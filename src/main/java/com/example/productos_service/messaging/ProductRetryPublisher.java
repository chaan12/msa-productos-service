package com.example.productos_service.messaging;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.productos_service.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProductRetryPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ProductRetryPublisher.class);
    private static final String SEND_EMAIL_PENDING_MESSAGE = "Pendiente de ejecutar el paso de envio de correo";
    private static final String UPDATE_RETRY_JOB_PENDING_MESSAGE =
            "Pendiente de ejecutar el paso de actualizacion del retry job";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public ProductRetryPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper,
            @Value("${broker.topics.products}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(Product product) {
        try {
            String payload = objectMapper.writeValueAsString(buildEnvelope(product));
            kafkaTemplate.send(topic, payload);
            logger.warn("Product retry message published. topic={}, nombre={}",
                    topic, product != null ? product.getNombre() : null);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar el payload de retry de producto", exception);
        }
    }

    private Map<String, Object> buildEnvelope(Product product) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("data", product);
        envelope.put("sendEmail", buildPendingStep(SEND_EMAIL_PENDING_MESSAGE));
        envelope.put("updateRetryJobs", buildPendingStep(UPDATE_RETRY_JOB_PENDING_MESSAGE));
        return envelope;
    }

    private Map<String, Object> buildPendingStep(String message) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("status", "PENDING");
        step.put("message", message);
        return step;
    }
}
