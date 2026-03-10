package com.example.productos_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.productos_service.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
}