package com.example.monitor.tcp;

import com.example.monitor.common.Config;

import java.io.*; import java.net.*;

public class TcpControlClient {
  private final int remoteNodeId; private final String host;
  public TcpControlClient(int remoteNodeId, String host){ this.remoteNodeId = remoteNodeId; this.host = host; }

  public String request(String msg, int timeoutMs) throws IOException {
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(host, Config.TCP_CONTROL_POR