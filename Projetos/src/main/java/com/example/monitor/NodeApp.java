package com.example.monitor;

import com.example.monitor.common.Config;
import com.example.monitor.common.LamportClock;
import com.example.monitor.multicast.MulticastPublisher;
import com.example.monitor.tcp.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeApp {
    public static void main(String[] args) throws Exception {
        Map<String, String> arg = parseArgs(args);
        int nodeId = Integer.parseInt(arg.getOrDefault("--id", "1"));
        int tcpPort = Integer.parseInt(arg.getOrDefault("--tcp", String.valueOf(Config.TCP_CONTROL_PORT + nodeId)));
        int grpcPort = Integer.parseInt(arg.getOrDefault("--grpc", "50051"));
        String peersStr = arg.getOrDefault("--peers", "");
        boolean localTest = Boolean.parseBoolean(arg.getOrDefault("--local-test", "false"));

        // IP da máquina local
        String localIP = InetAddress.getLocalHost().getHostAddress();

        // Configuração dos peers
        List<InetSocketAddress> peerAddrs = new ArrayList<>();
        Map<Integer, InetSocketAddress> peerMap = new HashMap<>();
        if (!peersStr.isBlank()) {
            for (String p : peersStr.split(",")) {
                String[] a = p.split(":");
                String host = a[0];
                int port = Integer.parseInt(a[1]);
                int peerId = Integer.parseInt(a[2]);

                // Se for teste local, substitui IP da própria máquina por "localhost"
                if (localTest && host.equals(localIP)) {
                    host = "localhost";
                }

                InetSocketAddress addr = new InetSocketAddress(host, port);
                peerAddrs.add(addr);
                peerMap.put(peerId, addr);
            }
        }

        // Inicialização dos componentes
        LamportClock clock = new LamportClock();
        MulticastPublisher publisher = new MulticastPublisher();

        // Serviço de eleição compartilhado
        EleicaoServiceBully electionService = new EleicaoServiceBully();
        List<BullyElection> electionProcesses = new ArrayList<>();

        // Criação dos componentes
        LeaderCoordinator coordinator = new LeaderCoordinator(nodeId, clock, peerAddrs, publisher);
        HeartbeatManager hb = new HeartbeatManager(nodeId, clock, peerMap);
        BullyElection bully = new BullyElection(nodeId, clock, peerMap.keySet(), electionProcesses, electionService);

        // Adiciona este nó à lista de processos
        electionProcesses.add(bully);

        TcpControlServer server = new TcpControlServer(tcpPort, nodeId, clock, bully, hb);
        GrpcServer grpc = new GrpcServer(grpcPort, nodeId, clock, coordinator);

        // Pool de threads para executar os serviços
        ExecutorService pool = Executors.newFixedThreadPool(4);
        pool.submit(server);
        pool.submit(hb);
        grpc.start();

        // Eleição inicial
        bully.startElection();
        coordinator.setLeaderId(bully.getCurrentLeader());
        pool.submit(coordinator);

        log("Node " + nodeId + " iniciado. Portas: TCP=" + tcpPort + " gRPC=" + grpcPort +
            "\nMulticast: " + Config.MULTICAST_GROUP + ":" + Config.MULTICAST_PORT +
            "\nLíder atual: " + bully.getCurrentLeader() +
            "\nModo de teste local: " + localTest);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log("Encerrando Node " + nodeId);
            try {
                coordinator.shutdown();
                hb.shutdown();
                grpc.stop();
                pool.shutdownNow();
            } catch (Exception e) {
                log("Erro durante shutdown: " + e.getMessage());
            }
        }));
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String value = (i + 1 < args.length && !args[i + 1].startsWith("--")) ? args[++i] : "true";
                params.put(arg, value);
            }
        }
        return params;
    }

    private static void log(String message) {
        System.out.println("[NodeApp] " + message);
    }
}
