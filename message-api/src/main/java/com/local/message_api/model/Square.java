package com.local.message_api.model;

public class Square {
    private String type = "Square";
    private long timestamp;

    public Square() {
        this.timestamp = System.currentTimeMillis();
    }

    public Square(long timestamp) {
        this.type = "Square";
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
        return "Square{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
