package com.local.message_api.model;

public class Circle {
    private String type = "Circle";
    private long timestamp;

    public Circle() {
        this.timestamp = System.currentTimeMillis();
    }

    public Circle(long timestamp) {
        this.type = "Circle";
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
