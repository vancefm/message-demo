package com.local.message_api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String aggregateId;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(nullable = false)
    private String routingKey;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private boolean published = false;
    
    @Column
    private LocalDateTime publishedAt;

    public OutboxEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public OutboxEvent(String aggregateId, String eventType, String payload, String routingKey) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.routingKey = routingKey;
        this.createdAt = LocalDateTime.now();
        this.published = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "id=" + id +
                ", aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", published=" + published +
                '}';
    }
}