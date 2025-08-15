package com.example.monitor.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.example.monitor.common.Config;

public class TcpControlClient {

    public String ask(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            socket.setSoTimeout(Config.SOCKET_TIMEOUT_MS);
            out.println(message);
            return in.readLine();

        } catch (IOException e) {
            if (!e.getMessage().contains("Connection refused")) {
                System.err.println("[TCP-Client] Erro: " + e.getMessage());
            }
            return null;
        }
    }

    public static void send(String host, int port, String message) {
        int maxRetries = 3;
        int retryDelayMs = 500;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                socket.setSoTimeout(Config.SOCKET_TIMEOUT_MS);
                out.println(message);
                return; // sucesso

            } catch (IOException e) {
                if (e.getMessage().contains("Connection refused")) {
                    System.out.println("[TCP-Client] Nó " + host + ":" + port + " indisponível");
                    return;
                }
                System.err.println("[TCP-Client] Falha (tentativa " + attempt + "): " + e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(retryDelayMs); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    public static boolean waitForServer(String host, int port, int timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (isNodeAvailable(host, port)) return true;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private static boolean isNodeAvailable(String host, int port) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 500); // timeout rápido
            try (PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                out.println("PING");
                String resp = in.readLine();
                return "PONG".equals(resp);
            }
        } catch (IOException e) {
            return false;
        }
    }

    public String sendCommand(String host, int port, String cmd) {
        if (!isNodeAvailable(host, port)) {
            System.out.println("[TCP-Client] Nó " + host + ":" + port + " indisponível");
            return null;
        }
        try (Socket s = new Socket(host, port);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            out.println(cmd);
            return in.readLine();

        } catch (IOException e) {
            System.out.println("[TCP-Client] Falha ao enviar comando para " + host + ":" + port + " -> " + e.getMessage());
            return null;
        }
    }
}
