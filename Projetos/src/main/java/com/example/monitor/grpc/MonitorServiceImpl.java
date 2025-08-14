package com.example.monitor.grpc;

import com.example.monitor.common.LamportClock;
import com.example.monitor.common.SystemMetrics;
import com.example.monitor.LeaderCoordinator;
import io.grpc.stub.StreamObserver;

import com.example.monitor.grpc.MonitorServiceGrpc;
import com.example.monitor.grpc.NodeStatus;
import com.example.monitor.grpc.StatusRequest;
import com.example.monitor.grpc.StatusResponse;
import com.example.monitor.grpc.MonitorProto.*;
import com.example.monitor.grpc.MonitorServiceGrpc.MonitorServiceImplBase;

public class MonitorServiceImpl extends MonitorServiceGrpc.MonitorServiceImplBase {
    private final int nodeId;
    private final LamportClock clock;
    private final LeaderCoordinator coordinator;

    public MonitorServiceImpl(int nodeId, LamportClock clock, LeaderCoordinator coordinator) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.coordinator = coordinator;
    }

    @Override
    public void getStatus(StatusRequest request, StreamObserver<StatusResponse> responseObserver) {
        clock.tick();
        
        NodeStatus nodeStatus = NodeStatus.newBuilder()
            .setNodeId(nodeId)
            .setCpu(SystemMetrics.cpuLoad())
            .setMem(SystemMetrics.memUsage())
            .setUptimeSec(SystemMetrics.uptimeSeconds())
            .setLamport(clock.now())
            .setAlive(true)
            .build();
        
        StatusResponse response = StatusResponse.newBuilder()
            .addNodes(nodeStatus)
            .setLeaderId(coordinator.currentLeaderId())
            .setLclock(clock.now())
            .setSnapshotId("snap-" + System.currentTimeMillis())
            .build();
            
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}