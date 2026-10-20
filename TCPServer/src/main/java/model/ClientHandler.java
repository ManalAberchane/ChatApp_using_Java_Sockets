package model;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private boolean readOnly;

    ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String name = in.readLine();
            if (name == null) return;
            name = name.trim();
            if (name.isEmpty()) {
                readOnly = true;
                username = server.nextGuestName();
                send("[Server] You are in READ-ONLY MODE. You can read but not send messages.");
            } else {
                username = name;
            }
            server.register(this);

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("end") || line.equalsIgnoreCase("bye")) {
                    break;
                }

                if (readOnly) {
                    send("[Server] READ-ONLY MODE: you cannot send messages.");
                } else if (line.equals("allUsers")) {
                    send("[Server] Active users: " + String.join(", ", server.usernames()));
                } else {
                    server.broadcast(server.formatMessage(username, line));
                }
            }
        } catch (IOException e) {
        } finally {
            if (username != null) server.remove(this);
            close();
        }
    }

    void send(String message) {
        if (out != null) out.println(message);
    }

    void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}