package com.local.message_api.model;

public class Pentagon {
    private String type = "Pentagon";
    private long timestamp;

    public Pentagon() {
        this.timestamp = System.currentTimeMillis();
    }

    public Pentagon(long timestamp) {
        this.type = "Pentagon";
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
        return "Pentagon{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
