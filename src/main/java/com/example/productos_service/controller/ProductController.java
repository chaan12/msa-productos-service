package com.example.productos_service.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.productos_service.model.Product;
import com.example.productos_service.messaging.ProductRetryPublisher;
import com.example.productos_service.service.ProductService;

@RestController
@RequestMapping("/productos")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final ProductRetryPublisher productRetryPublisher;

    public ProductController(ProductService productService, ProductRetryPublisher productRetryPublisher){
        this.productService = productService;
        this.productRetryPublisher = productRetryPublisher;
    }

    @GetMapping
    public List<Product> obtenerProductos(){
        return productService.obtenerProductos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> obtenerProducto(@PathVariable String id){
        return ResponseEntity.ok(productService.obtenerProductoPorId(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearProducto(@RequestBody Product product){
        try {
            Product savedProduct = productService.crearProducto(product);
            logger.info("Producto creado correctamente. id={}, nombre={}", savedProduct.getId(), savedProduct.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(buildResponse(true, "Producto creado correctamente", savedProduct));
        } catch (ResponseStatusException exception) {
            publishRetryOnServerError(product, exception);
            throw exception;
        } catch (Exception exception) {
            publishRetryJob(product, exception);
            throw exception;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> actualizarProducto(@PathVariable String id, @RequestBody Product product){
        return ResponseEntity.ok(productService.actualizarProducto(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String id){
        productService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> buildResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private void publishRetryOnServerError(Product product, ResponseStatusException exception) {
        if (exception.getStatusCode().is5xxServerError()) {
            publishRetryJob(product, exception);
        }
    }

    private void publishRetryJob(Product product, Exception exception) {
        logger.warn("Publishing product retry job after create failure. nombre={}, error={}",
                product != null ? product.getNombre() : null, exception.getMessage());
        productRetryPublisher.publish(product);
    }
}
