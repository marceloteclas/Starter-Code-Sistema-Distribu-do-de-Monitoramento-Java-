package com.example.monitor.tcp;

import com.example.monitor.common.Config; // Adicione este import
import com.example.monitor.common.LamportClock;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

public class HeartbeatManager implements Runnable {
    private final int nodeId;
    private final LamportClock clock;
    private final Map<Integer, InetSocketAddress> peers;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean running = true;

    public HeartbeatManager(int nodeId, LamportClock clock, Map<Integer, InetSocketAddress> peers) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.peers = peers;
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeats, 
            0, Config.HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeats() {
        if (!running) return;
        
        peers.forEach((id, addr) -> {
            if (id != nodeId) {
                try {
                    String message = "HEARTBEAT:" + nodeId + ":" + clock.tick();
                    TcpControlClient.send(addr.getHostString(), addr.getPort(), message);
                } catch (Exception e) {
                    System.err.println("Heartbeat to node " + id + " failed: " + e.getMessage());
                }
            }
        });
    }

    public void shutdown() {
        running = false;
        scheduler.shutdown();
    }
}