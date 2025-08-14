package com.example.monitor.common;

import java.net.InetAddress;
import java.util.List;

public class Config {
  public static final int TCP_CONTROL_PORT = 7000; // entre nós
  public static final int GRPC_PORT = 50051;       // serviços
  public static final String MULTICAST_GROUP = "230.0.0.1";
  public static final int MULTICAST_PORT = 4446;
  public static final int HEARTBEAT_INTERVAL_MS = 1000;
  public static final int HEARTBEAT_MISSES_TO_FAIL = 3;
  public static final int LEADER_POLL_MS = 2000;

  public static record NodeInfo(int id, String host) {}

  // Lista estática de nós para simplificar; pode vir de arquivo/arg
  public static List<NodeInfo> cluster() {
    return List.of(
      new NodeInfo(1, "127.0.0.1"),
      new NodeInfo(2, "127.0.0.1"),
      new NodeInfo(3, "127.0.0.1")
    );
  }

  public static String myHost() {
    try { return InetAddress.getLocalHost().getHostAddress(); } catch (Exception e) { return "127.0.0.1"; }
  }
}