package com.example.productos_service.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.productos_service.model.Product;
import com.example.productos_service.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> obtenerProductos() {
        return productRepository.findAll();
    }

    public Product obtenerProductoPorId(String id) {
        validateId(id, "El id del producto es obligatorio");
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public Product crearProducto(Product product) {
        validateProduct(product);
        product.setId(null);
        product.setNombre(product.getNombre().trim());
        return productRepository.save(product);
    }

    public Product actualizarProducto(String id, Product product) {
        validateProduct(product);
        Product existingProduct = obtenerProductoPorId(id);
        existingProduct.setNombre(product.getNombre().trim());
        existingProduct.setPrecio(product.getPrecio());
        return productRepository.save(existingProduct);
    }

    public void eliminarProducto(String id) {
        Product existingProduct = obtenerProductoPorId(id);
        productRepository.delete(existingProduct);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        if (product.getNombre() == null || product.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio");
        }
        if (product.getPrecio() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto no puede ser negativo");
        }
    }

    private void validateId(String id, String message) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
