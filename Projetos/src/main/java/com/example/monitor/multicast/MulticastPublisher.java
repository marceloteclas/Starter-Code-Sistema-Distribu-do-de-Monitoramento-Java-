package com.example.monitor.multicast;

import com.example.monitor.common.Config;

import java.io.IOException;
import java.net.*;

public class MulticastPublisher {
    private final InetAddress group;
    private final int port;
    private final DatagramSocket socket;

    public MulticastPublisher() {
        try {
            this.group = InetAddress.getByName(Config.MULTICAST_GROUP);
            this.port = Config.MULTICAST_PORT;
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void publish(String text) {
        try {
            byte[] buf = text.getBytes();
            DatagramPacket p = new DatagramPacket(buf, buf.length, group, port);
            socket.send(p);
        } catch (IOException e) {
            System.out.println("[MCST] erro: " + e.getMessage());
        }
    }
}
