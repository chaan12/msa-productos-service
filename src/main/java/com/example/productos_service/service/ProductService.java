package com.example.productos_service.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.productos_service.dto.InventoryUpdateEvent;
import com.example.productos_service.dto.InventoryUpdateItem;
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
        product.setName(product.getName().trim());
        return productRepository.save(product);
    }

    public Product actualizarProducto(String id, Product product) {
        validateProduct(product);
        Product existingProduct = obtenerProductoPorId(id);
        existingProduct.setName(product.getName().trim());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setImage(product.getImage());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setSubcategory(product.getSubcategory());
        existingProduct.setBrand(product.getBrand());
        existingProduct.setSupplier(product.getSupplier());
        return productRepository.save(existingProduct);
    }

    public void eliminarProducto(String id) {
        Product existingProduct = obtenerProductoPorId(id);
        productRepository.delete(existingProduct);
    }

    public void aplicarActualizacionInventario(InventoryUpdateEvent event) {
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El evento de inventario es obligatorio");
        }
        validateId(event.getEventId(), "El eventId es obligatorio");
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        for (InventoryUpdateItem item : event.getItems()) {
            aplicarItemInventario(event.getEventId(), item);
        }
    }

    private void aplicarItemInventario(String eventId, InventoryUpdateItem item) {
        if (item == null) {
            return;
        }
        validateId(item.getProductId(), "El productId del evento de inventario es obligatorio");
        if (item.getQuantityDelta() == null || Objects.equals(item.getQuantityDelta(), 0)) {
            return;
        }

        Product product = obtenerProductoPorId(item.getProductId().trim());
        if (product.getProcessedInventoryEventIds().contains(eventId)) {
            return;
        }

        int currentQuantity = product.getQuantity() == null ? 0 : product.getQuantity();
        int updatedQuantity = currentQuantity + item.getQuantityDelta();
        if (updatedQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para el producto " + product.getId());
        }

        product.setQuantity(updatedQuantity);
        product.getProcessedInventoryEventIds().add(eventId);
        productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio");
        }
        if (product.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto no puede ser negativo");
        }
        if (product.getQuantity() != null && product.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad del producto no puede ser negativa");
        }
    }

    private void validateId(String id, String message) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
