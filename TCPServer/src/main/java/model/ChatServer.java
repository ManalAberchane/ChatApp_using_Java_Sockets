package model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {
    private final int port;
    private final ServerListener listener;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicInteger guestCounter = new AtomicInteger(1);
    private ServerSocket serverSocket;
    private volatile boolean running;

    public ChatServer(int port, ServerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        listener.onLog("Server Started on port " + port);
        Thread.ofVirtual().start(this::acceptLoop);
    }

    private void acceptLoop() {
        while (running) {
            try {
                listener.onLog("Waiting for Client...");
                Socket socket = serverSocket.accept();
                Thread.ofVirtual().start(new ClientHandler(socket, this));
            } catch (IOException e) {
                if (running) listener.onLog("Accept error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        for (ClientHandler c : clients) c.close();
    }


    void register(ClientHandler handler) {
        clients.add(handler);
        listener.onLog("Welcome " + handler.getUsername());
        listener.onUsersChanged(usernames());
        broadcast("[Server] " + handler.getUsername() + " joined the chat");
    }

    void remove(ClientHandler handler) {
        if (clients.remove(handler)) {
            listener.onLog(handler.getUsername() + " disconnected");
            listener.onUsersChanged(usernames());
            broadcast("[Server] " + handler.getUsername() + " left the chat");
        }
    }

    void broadcast(String message) {
        listener.onLog(message);
        for (ClientHandler c : clients) c.send(message);
    }

    String formatMessage(String username, String text) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        return "[" + time + "] " + username + ": " + text;
    }

    List<String> usernames() {
        return clients.stream().map(ClientHandler::getUsername).toList();
    }

    String nextGuestName() {
        return "Guest-" + guestCounter.getAndIncrement();
    }
}