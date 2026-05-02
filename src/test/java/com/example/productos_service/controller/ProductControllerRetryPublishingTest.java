package com.example.productos_service.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.productos_service.messaging.ProductRetryPublisher;
import com.example.productos_service.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
@Import(ProductRetryPublisher.class)
@TestPropertySource(properties = "broker.topics.products=product_retry_jobs")
class ProductControllerRetryPublishingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldPublishProductRetryMessageWhenUnexpectedErrorOccurs() throws Exception {
        when(productService.crearProducto(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("Mongo no disponible"));

        mockMvc.perform(post("/productos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Auriculares Bluetooth",
                                  "description": "Auriculares inalambricos con cancelacion de ruido",
                                  "price": 79.99,
                                  "quantity": 120,
                                  "image": "https://cdn.example.com/products/auriculares-bt.jpg",
                                  "category": "Electronica",
                                  "subcategory": "Audio",
                                  "brand": "SoundMax",
                                  "supplier": "Distribuidora Central SA",
                                  "id": "39d41c02-4ede-48f0-9653-ba295681af9e"
                                }
                                """))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("product_retry_jobs"), payloadCaptor.capture());

        JsonNode root = objectMapper.readTree(payloadCaptor.getValue());
        JsonNode data = root.get("data");

        org.junit.jupiter.api.Assertions.assertEquals(1, root.size());
        org.junit.jupiter.api.Assertions.assertEquals("Auriculares Bluetooth", data.get("name").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Auriculares inalambricos con cancelacion de ruido",
                data.get("description").asText());
        org.junit.jupiter.api.Assertions.assertEquals(79.99d, data.get("price").asDouble());
        org.junit.jupiter.api.Assertions.assertEquals(120, data.get("quantity").asInt());
        org.junit.jupiter.api.Assertions.assertEquals("https://cdn.example.com/products/auriculares-bt.jpg",
                data.get("image").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Electronica", data.get("category").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Audio", data.get("subcategory").asText());
        org.junit.jupiter.api.Assertions.assertEquals("SoundMax", data.get("brand").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Distribuidora Central SA", data.get("supplier").asText());
        org.junit.jupiter.api.Assertions.assertEquals("39d41c02-4ede-48f0-9653-ba295681af9e",
                data.get("id").asText());
        org.junit.jupiter.api.Assertions.assertFalse(root.has("sendEmail"));
        org.junit.jupiter.api.Assertions.assertFalse(root.has("updateRetryJobs"));
    }

    @Test
    void shouldNotPublishRetryMessageWhenClientErrorOccurs() throws Exception {
        when(productService.crearProducto(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio"));

        mockMvc.perform(post("/productos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "price": 499.99
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(kafkaTemplate, never()).send(eq("product_retry_jobs"), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldNotRepublishRetryMessageWhenRequestComesFromBroker() throws Exception {
        when(productService.crearProducto(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("Mongo no disponible"));

        mockMvc.perform(post("/productos")
                        .header("X-Broker-Retry", "true")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Monitor 24 pulgadas",
                                  "price": 249.99
                                }
                                """))
                .andExpect(status().isInternalServerError());

        verify(kafkaTemplate, never()).send(eq("product_retry_jobs"), org.mockito.ArgumentMatchers.anyString());
    }
}
