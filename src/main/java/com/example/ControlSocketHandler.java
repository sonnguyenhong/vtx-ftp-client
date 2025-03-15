package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ControlSocketHandler {
    private BufferedReader reader;
    private PrintWriter writer;
    private final Object lock = new Object();
    
    public ControlSocketHandler(Socket socket) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String command) {
        this.writer.println(command);
    }

    public String receive() throws IOException {
        String response = this.reader.readLine();
        return response;
    }

    public Object getLock() {
        return lock;
    }
}
