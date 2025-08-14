package com.example.monitor;

import com.example.monitor.common.Config;
import com.example.monitor.common.LamportClock;
import com.example.monitor.common.NodeStatus;
import com.example.monitor.common.SystemMetrics;
import com.example.monitor.multicast.MulticastPublisher;
import com.example.monitor.tcp.TcpControlClient;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LeaderCoordinator implements Runnable {
    private final int myId; // Mudamos para int para consistência
    private final LamportClock clock;
    private final TcpControlClient client = new TcpControlClient();
    private final MulticastPublisher publisher;
    private volatile boolean running = true;
    private final List<InetSocketAddress> peers;
    private final AtomicLong seq = new AtomicLong(0);
    private volatile Integer leaderId;

    public LeaderCoordinator(int myId, LamportClock clock,
                           List<InetSocketAddress> peers,
                           MulticastPublisher publisher) {
        this.myId = myId;
        this.clock = clock;
        this.peers = peers;
        this.publisher = publisher;
        this.leaderId = null;
    }

    public void setLeaderId(Integer id){ this.leaderId = id; }
    public Integer currentLeaderId(){ return leaderId==null? -1: leaderId; }

    @Override
    public void run() {
        while (running) {
            try {
                if (leaderId != null && leaderId == myId) {
                    doLeaderCycle();
                }
                Thread.sleep(Config.SNAPSHOT_INTERVAL_MS);
            } catch (InterruptedException ignored) {}
        }
    }

     private void doLeaderCycle() {
        long s = seq.incrementAndGet();
        long ts = clock.tick(); // Usando o método tick()
        
        NodeStatus local = new NodeStatus(
            myId,
            SystemMetrics.cpuLoad(),
            SystemMetrics.memUsage(),
            SystemMetrics.uptimeSeconds(),
            clock.now(), // Usando now()
            true
        );
        
        Map<Integer, NodeStatus> latest = new LinkedHashMap<>();
        latest.put(myId, local);
        // requisita aos peers vivos
        for (InetSocketAddress p : peers) {
            try {
                String line = "SNAPREQ:" + s + ":" + ts;
                String r = client.ask(p.getHostString(), p.getPort(), line);
                if (r != null && r.startsWith("SNAPREP:")) {
                    String[] a = r.split(":");
                    long seqR = Long.parseLong(a[1]);
                    if (seqR != s) continue;
                    NodeStatus status = NodeStatus.fromCompactString(a[2]);
                    latest.put(status.nodeId(), status);
                }
            } catch (Exception ignored) {}
        }

        // publica no multicast
        StringBuilder payload = new StringBuilder();
        payload.append("SEQ=").append(s)
              .append(" L=").append(clock.now())
              .append(" LEADER=").append(myId);
        for (NodeStatus ns : latest.values()) {
            payload.append(" | ").append(ns.toCompactString());
        }
        publisher.publish(payload.toString());
        log("publish: " + payload);
    }

    public void shutdown(){ running = false; }

    private static void log(String s){ System.out.println("[SNAP] " + s); }
}
