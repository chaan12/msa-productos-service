package com.example.productos_service.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.productos_service.dto.InventoryUpdateEvent;
import com.example.productos_service.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class InventoryUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryUpdateConsumer.class);

    private final ObjectMapper objectMapper;
    private final ProductService productService;

    public InventoryUpdateConsumer(ObjectMapper objectMapper, ProductService productService) {
        this.objectMapper = objectMapper;
        this.productService = productService;
    }

    @KafkaListener(topics = "${kafka.topics.inventory-updates:inventory_update_events}")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            InventoryUpdateEvent event = objectMapper.readValue(record.value(), InventoryUpdateEvent.class);
            productService.aplicarActualizacionInventario(event);
            logger.info("Inventory event applied. topic={}, offset={}, eventId={}, orderId={}",
                    record.topic(), record.offset(), event.getEventId(), event.getOrderId());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo deserializar el evento de inventario", exception);
        }
    }
}
