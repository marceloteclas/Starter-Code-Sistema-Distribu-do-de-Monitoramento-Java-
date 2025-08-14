package com.example.monitor.tcp;

import com.example.monitor.common.LamportClock;
import com.example.monitor.common.NodeStatus;
import com.example.monitor.common.SystemMetrics;
import com.example.monitor.common.Config;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TcpControlServer implements Runnable {
  private final int nodeId; private final LamportClock clock; private volatile boolean running = true;
  private final ExecutorService pool = Executors.newCachedThreadPool();
  private final BullyElection bully; private final HeartbeatManager hbManager;

  public TcpControlServer(int nodeId, LamportClock clock, BullyElection bully, HeartbeatManager hbManager) {
    this.nodeId = nodeId; this.clock = clock; this.bully = bully; this.hbManager = hbManager;
  }

  @Override public void run() {
    try (ServerSocket ss = new ServerSocket(Config.TCP_CONTROL_PORT + nodeId)) {
      while (running) { Socket s = ss.accept(); pool.execute(() -> handle(s)); }
    } catch (IOException e) { /* log */ }
  }

  private void handle(Socket s) {
    try (s; var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         var out = new PrintWriter(s.getOutputStream(), true)) {
        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.split("\\|", 2);
            String type = parts[0];
            switch (type) {
                case "HEARTBEAT" -> { 
                    out.println("HEARTBEAT_ACK|" + clock.tick()); 
                }
                case "JOIN" -> { 
                    out.println("JOIN_OK|" + nodeId); 
                }
                case "ELECTION" -> { 
                    String senderId = parts.length > 1 ? parts[1] : "";
                    bully.processElectionMessage(senderId);
                    out.println("ELECTION_ACK|" + nodeId); 
                }
                case "COORDINATOR" -> { 
                    String leaderId = parts.length > 1 ? parts[1] : "";
                    bully.processCoordinatorMessage(leaderId);
                    out.println("COORDINATOR_ACK|" + nodeId); 
                }
                case "GET_LOCAL_STATUS" -> {
                    NodeStatus ns = new NodeStatus(
                        nodeId, 
                        SystemMetrics.cpuLoad(), 
                        SystemMetrics.memUsage(), 
                        SystemMetrics.uptimeSeconds(), 
                        clock.tick(), 
                        true
                    );
                    out.println("STATUS|" + ns.toJson());
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Error handling TCP connection: " + e.getMessage());
    }
}
}