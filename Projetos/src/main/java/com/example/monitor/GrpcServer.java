package com.example.monitor;

import com.example.monitor.common.LamportClock;
import com.example.monitor.grpc.AuthServiceImpl;
import com.example.monitor.grpc.MonitorServiceImpl;
import com.example.monitor.LeaderCoordinator;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class GrpcServer {
    private final Server server;
    
    public GrpcServer(int port, int nodeId, LamportClock clock, LeaderCoordinator coordinator) {
        this.server = ServerBuilder.forPort(port)
            .addService(new AuthServiceImpl())
            .addService(new MonitorServiceImpl(nodeId, clock, coordinator))
            .build();
    }
    
    public void start() throws IOException {
        server.start();
        System.out.println("gRPC Server started on port " + server.getPort());
    }
    
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}