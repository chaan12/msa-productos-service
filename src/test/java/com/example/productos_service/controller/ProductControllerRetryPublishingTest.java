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
                                  "nombre": "Monitor 4K",
                                  "precio": 499.99
                                }
                                """))
                .andExpect(status().isInternalServerError());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("product_retry_jobs"), payloadCaptor.capture());

        JsonNode root = objectMapper.readTree(payloadCaptor.getValue());
        JsonNode data = root.get("data");

        org.junit.jupiter.api.Assertions.assertEquals("Monitor 4K", data.get("nombre").asText());
        org.junit.jupiter.api.Assertions.assertEquals(499.99d, data.get("precio").asDouble());
        org.junit.jupiter.api.Assertions.assertEquals("PENDING", root.get("sendEmail").get("status").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Pendiente de ejecutar el paso de envio de correo",
                root.get("sendEmail").get("message").asText());
        org.junit.jupiter.api.Assertions.assertEquals("PENDING", root.get("updateRetryJobs").get("status").asText());
        org.junit.jupiter.api.Assertions.assertEquals("Pendiente de ejecutar el paso de actualizacion del retry job",
                root.get("updateRetryJobs").get("message").asText());
    }

    @Test
    void shouldNotPublishRetryMessageWhenClientErrorOccurs() throws Exception {
        when(productService.crearProducto(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio"));

        mockMvc.perform(post("/productos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "precio": 499.99
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(kafkaTemplate, never()).send(eq("product_retry_jobs"), org.mockito.ArgumentMatchers.anyString());
    }
}
