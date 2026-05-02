package com.example.productos_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryUpdateEvent {

    private String eventId;
    private String eventType;
    private String orderId;
    private String status;
    private List<InventoryUpdateItem> items;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<InventoryUpdateItem> getItems() {
        return items;
    }

    public void setItems(List<InventoryUpdateItem> items) {
        this.items = items;
    }
}
