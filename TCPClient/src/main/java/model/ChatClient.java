package model;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private final MessageListener listener;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean connected;

    public ChatClient(MessageListener listener) {
        this.listener = listener;
    }


    public void connect(String host, int port, String username) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        connected = true;

        out.println(username == null ? "" : username.trim());

        Thread.ofVirtual().start(this::readLoop);
    }

    private void readLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                listener.onMessage(line);
            }
        } catch (IOException ignored) {
            // connection lost
        } finally {
            if (connected) {
                connected = false;
                close();
                listener.onDisconnected();
            }
        }
    }

    public void send(String text) {
        if (connected) out.println(text);
    }

    public void disconnect() {
        if (!connected) return;
        out.println("bye");
        connected = false;
        close();
        listener.onDisconnected();
    }

    public boolean isConnected() {
        return connected;
    }

    private void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}