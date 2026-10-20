import model.ChatServer;
import model.Config;
import model.ServerListener;

import java.util.List;

public class TCPServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(Config.load().getProperty("server.port", "3000"));

        ChatServer server = new ChatServer(port, new ServerListener() {
            @Override
            public void onLog(String message) {
                System.out.println(message);
            }

            @Override
            public void onUsersChanged(List<String> usernames) {
                System.out.println("Users: " + usernames);
            }
        });

        server.start();

        // Keep the program alive: virtual threads don't stop the JVM from exiting
        Thread.currentThread().join();
    }
}