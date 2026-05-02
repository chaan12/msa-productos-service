package com.example.productos_service.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "productos")
public class Product {

    @Id
    private String id;

    @JsonAlias("nombre")
    private String name;

    private String description;

    @JsonAlias("precio")
    private double price;

    private Integer quantity;
    private String image;
    private String category;
    private String subcategory;
    private String brand;
    private String supplier;

    @JsonIgnore
    private Set<String> processedInventoryEventIds = new HashSet<>();

    public Product() {}

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Set<String> getProcessedInventoryEventIds() {
        if (processedInventoryEventIds == null) {
            processedInventoryEventIds = new HashSet<>();
        }
        return processedInventoryEventIds;
    }

    public void setProcessedInventoryEventIds(Set<String> processedInventoryEventIds) {
        this.processedInventoryEventIds = processedInventoryEventIds;
    }
}
