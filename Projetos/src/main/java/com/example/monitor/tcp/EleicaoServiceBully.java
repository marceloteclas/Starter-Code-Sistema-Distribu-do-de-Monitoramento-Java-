package com.example.monitor.tcp;

import java.util.List;

public class EleicaoServiceBully {
    /**
     * Inicia o algoritmo de eleição Bully.
     */
    public void iniciarEleicao(BullyElection solicitante, List<BullyElection> todosProcessos) {
        System.out.println("[Bully] Node " + solicitante.getNodeId() + " iniciando eleição...");

        boolean respostaRecebida = false;

        // Envia mensagens para todos os processos com ID maior
        for (BullyElection processo : todosProcessos) {
            if (processo.getNodeId() > solicitante.getNodeId() && processo.isActive()) {
                System.out.println("[Bully] Node " + solicitante.getNodeId() + 
                                 " enviando ELEICAO para Node " + processo.getNodeId());
                respostaRecebida = true;
                
                // Nota: A comunicação real é feita via TcpControlServer
            }
        }

        // Se nenhum processo com ID maior respondeu, declare-se líder
        if (!respostaRecebida) {
            System.out.println("[Bully] Nenhum nó superior respondeu, Node " + 
                             solicitante.getNodeId() + " assumindo liderança");
            solicitante.declareVictory();
        }
    }

    /**
     * Notifica todos os processos sobre o novo coordenador.
     */
    public void notificarCoordenador(BullyElection coordenador, List<BullyElection> todosProcessos) {
        for (BullyElection processo : todosProcessos) {
            if (processo.getNodeId() != coordenador.getNodeId() && processo.isActive()) {
                System.out.println("[Bully] Notificando Node " + processo.getNodeId() + 
                                 " sobre novo líder: " + coordenador.getNodeId());
                // Nota: A comunicação real é feita via TcpControlServer
                processo.processCoordinatorMessage(String.valueOf(coordenador.getNodeId()));
            }
        }
    }
}