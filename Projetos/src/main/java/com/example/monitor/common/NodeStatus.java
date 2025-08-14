package com.example.monitor.common;

public record NodeStatus(int nodeId, double cpu, double mem, long uptimeSeconds, long lamport, boolean alive) {
    public String toJson() {
        return String.format("{\"nodeId\":%d,\"cpu\":%.4f,\"mem\":%.4f,\"uptimeSec\":%d,\"lamport\":%d,\"alive\":%b}",
            nodeId, cpu, mem, uptimeSeconds, lamport, alive);
    }
    
    public String toCompactString() {
        return String.format("%d:%.2f:%.2f:%d:%d:%b",
            nodeId, cpu, mem, uptimeSeconds, lamport, alive);
    }
    
    public static NodeStatus fromCompactString(String compact) {
        String[] parts = compact.split(":");
        return new NodeStatus(
            Integer.parseInt(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Long.parseLong(parts[3]),
            Long.parseLong(parts[4]),
            Boolean.parseBoolean(parts[5])
        );
    }
}