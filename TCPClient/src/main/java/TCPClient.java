import model.ChatClient;
import model.MessageListener;

import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java TCPClient <ServerIPAddress> <PortNumber>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Scanner sc = new Scanner(System.in);
        System.out.print("Username (empty = read-only): ");
        String name = sc.nextLine();

        ChatClient client = new ChatClient(new MessageListener() {
            @Override
            public void onMessage(String text) {
                System.out.println(text);
            }

            @Override
            public void onDisconnected() {
                System.out.println("Disconnected.");
                System.exit(0);
            }
        });
        client.connect(host, port, name);

        while (client.isConnected()) {
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("end") || line.equalsIgnoreCase("bye")) {
                client.disconnect();
            } else {
                client.send(line);
            }
        }
    }
}