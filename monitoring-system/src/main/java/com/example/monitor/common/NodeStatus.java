package com.example.monitor.common;

public record NodeStatus(int nodeId, double cpu, double mem, long uptimeSec, long lamport, boolean alive) {
  public String toJson() {
    return String.format("{\"nodeId\":%d,\"cpu\":%.4f,\"mem\":%.4f,\"uptimeSec\":%d,\"lamport\":%d,\"alive\":%b}",
      nodeId, cpu, mem, uptimeSec, lamport, alive);
  }
}