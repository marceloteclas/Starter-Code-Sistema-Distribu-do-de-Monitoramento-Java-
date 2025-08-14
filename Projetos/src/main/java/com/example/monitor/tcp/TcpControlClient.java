package com.example.monitor.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.example.monitor.common.Config;  // Import adicionado

public class TcpControlClient {
    public String ask(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            socket.setSoTimeout(Config.SOCKET_TIMEOUT_MS);
            out.println(message);
            return in.readLine();
        } catch (IOException e) {
            System.err.println("TCP Client error: " + e.getMessage());
            return null;
        }
    }

    public static void send(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            socket.setSoTimeout(Config.SOCKET_TIMEOUT_MS);
            out.println(message);
        } catch (IOException e) {
            System.err.println("TCP Send error: " + e.getMessage());
        }
    }
}