package com.example.productos_service.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.productos_service.model.Product;
import com.example.productos_service.repository.ProductRepository;

@RestController
@RequestMapping("/productos")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductRepository repo;

    public ProductController(ProductRepository repo){
        this.repo = repo;
    }

    @GetMapping
    public List<Product> obtenerProductos(){
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Product obtenerProducto(@PathVariable String id){
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearProducto(@RequestBody Product product){
        try {
            Product savedProduct = repo.save(product);
            logger.info("Producto creado correctamente. id={}, nombre={}", savedProduct.getId(), savedProduct.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(true, "Producto creado correctamente", savedProduct));
        } catch (Exception exception) {
            logger.error("Error al crear producto. nombre={}", product.getNombre(), exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(false, "No se pudo crear el producto", null));
        }
    }

    @PutMapping("/{id}")
    public Product actualizarProducto(@PathVariable String id,@RequestBody Product product){
        product.setId(id);
        return repo.save(product);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable String id){
        repo.deleteById(id);
    }

    private Map<String, Object> buildResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
