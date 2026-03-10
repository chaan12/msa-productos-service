package com.example.productos_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.productos_service.model.Product;
import com.example.productos_service.repository.ProductRepository;

@RestController
@RequestMapping("/productos")
public class ProductController {

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
    public Product crearProducto(@RequestBody Product product){
        return repo.save(product);
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

}