package com.example.monitor.tcp;

import com.example.monitor.common.LamportClock;
import com.example.monitor.common.NodeStatus;
import com.example.monitor.common.SystemMetrics;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TcpControlServer implements Runnable {
    private final int nodeId;
    private final LamportClock clock;
    private volatile boolean running = true;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final BullyElection bully;
    private final HeartbeatManager hbManager;
    private final int tcpPort;

    public TcpControlServer(int tcpPort, int nodeId, LamportClock clock, BullyElection bully, HeartbeatManager hbManager) {
        this.tcpPort = tcpPort;
        this.nodeId = nodeId;
        this.clock = clock;
        this.bully = bully;
        this.hbManager = hbManager;
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(tcpPort, 50, InetAddress.getByName("0.0.0.0"))) {
            while (running) {
                Socket s = ss.accept();
                pool.execute(() -> handle(s));
            }
        } catch (IOException e) {
            System.err.println("[TCP-Server] Erro no servidor: " + e.getMessage());
        }
    }

    private void handle(Socket s) {
        try (s;
             var in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             var out = new PrintWriter(s.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                String type = parts[0];

                switch (type) {
                    case "PING" -> out.println("PONG");
                    case "HEARTBEAT" -> out.println("HEARTBEAT_ACK|" + clock.tick());
                    case "JOIN" -> out.println("JOIN_OK|" + nodeId);
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
                    default -> out.println("ERROR|Unknown command: " + type);
                }
            }

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (!msg.contains("Connection reset") &&
                !msg.contains("software no computador host")) {
                System.err.println("[TCP-Server] Erro ao lidar com conexão: " + msg);
            }
        }
    }
}
