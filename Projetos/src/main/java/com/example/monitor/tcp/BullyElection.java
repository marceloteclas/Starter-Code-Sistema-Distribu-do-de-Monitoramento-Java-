package com.example.monitor.tcp;

import com.example.monitor.common.LamportClock;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BullyElection {
    private final int nodeId;
    private final LamportClock clock;
    private final Set<Integer> peerIds;
    private final AtomicInteger currentLeader = new AtomicInteger(-1);
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final List<BullyElection> allProcesses;
    private final EleicaoServiceBully electionService;
    private boolean isCoordinator = false;

    public BullyElection(int nodeId, LamportClock clock, Set<Integer> peerIds,
                       List<BullyElection> allProcesses, EleicaoServiceBully electionService) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.peerIds = peerIds;
        this.allProcesses = allProcesses;
        this.electionService = electionService;
        this.allProcesses.add(this); // Auto-registro
    }

    /**
     * Processa mensagem de eleição recebida via TCP
     * @param senderIdStr ID do nó remetente como string
     */
    public void processElectionMessage(String senderIdStr) {
        if (!active.get()) return;
        
        try {
            int senderId = Integer.parseInt(senderIdStr);
            clock.tick(); // Atualiza relógio lógico
            
            System.out.println("[Bully] Node " + nodeId + " received ELECTION from " + senderId);
            
            if (senderId < nodeId) {
                // 1. Responde ao sender que está vivo
                // (A resposta é enviada pelo TcpControlServer)
                
                // 2. Inicia sua própria eleição
                startElection();
            }
        } catch (NumberFormatException e) {
            System.err.println("[Bully] Invalid election message format: " + senderIdStr);
        }
    }

    /**
     * Processa mensagem de novo coordenador
     * @param leaderIdStr ID do novo líder como string
     */
    public void processCoordinatorMessage(String leaderIdStr) {
        try {
            int newLeader = Integer.parseInt(leaderIdStr);
            clock.tick(); // Atualiza relógio lógico
            
            currentLeader.set(newLeader);
            isCoordinator = (newLeader == nodeId);
            
            System.out.println("[Bully] Node " + nodeId + " acknowledges new leader: " + newLeader);
        } catch (NumberFormatException e) {
            System.err.println("[Bully] Invalid coordinator message format: " + leaderIdStr);
        }
    }

    /**
     * Inicia o processo de eleição
     */
    public void startElection() {
        if (!active.get()) return;
        
        System.out.println("[Bully] Node " + nodeId + " starting election");
        electionService.iniciarEleicao(this, allProcesses);
    }

    /**
     * Declara vitória na eleição (chamado pelo EleicaoServiceBully)
     */
    public void declareVictory() {
        currentLeader.set(nodeId);
        isCoordinator = true;
        System.out.println("[Bully] Node " + nodeId + " declares victory");
        
        // Notificação será feita pelo EleicaoServiceBully
    }

    /**
     * Verifica se o líder atual está ativo
     */
    public boolean isLeaderActive() {
        if (currentLeader.get() == -1) return false;
        
        for (BullyElection process : allProcesses) {
            if (process.nodeId == currentLeader.get() && process.isActive()) {
                return true;
            }
        }
        return false;
    }

    // Métodos de estado
    public boolean isActive() {
        return active.get();
    }

    public void activate() {
        active.set(true);
        System.out.println("[Bully] Node " + nodeId + " activated");
    }

    public void deactivate() {
        active.set(false);
        isCoordinator = false;
        System.out.println("[Bully] Node " + nodeId + " deactivated");
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public int getCurrentLeader() {
        return currentLeader.get();
    }

    public int getNodeId() {
        return nodeId;
    }
}