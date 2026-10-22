package view;

import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.ListCell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import model.ChatServer;
import model.Config;
import model.ServerListener;

import java.util.List;

public class ServerApp extends Application implements ServerListener {
    private TextFlow logFlow;
    private ScrollPane logScroll;
    private static final Pattern CHAT_LINE = Pattern.compile("^(\\[\\d{2}:\\d{2}\\]) ([^:]+): (.*)$");
    private ListView<String> userList;
    private final ObservableList<String> users = FXCollections.observableArrayList();
    private ChatServer server;

    @Override
    public void start(Stage stage) {
        logFlow = new TextFlow();
        logFlow.setPadding(new Insets(6));
        logScroll = new ScrollPane(logFlow);
        logScroll.setFitToWidth(true);
        // Auto-scroll to the bottom when new lines arrive
        logFlow.heightProperty().addListener((obs, oldV, newV) -> logScroll.setVvalue(1.0));

        userList = new ListView<>(users);
        userList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(name);
                    String color = UserColors.pastelHex(name);
                    setStyle("-fx-background-color: " + color + "; -fx-text-fill: black;");
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(12));
        grid.setHgap(12);
        grid.setVgap(8);

        grid.add(new Label("Server Log"), 0, 0);
        grid.add(new Label("Connected Users"), 1, 0);
        grid.add(logScroll, 0, 1);
        grid.add(userList, 1, 1);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(70);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(30);
        grid.getColumnConstraints().addAll(left, right);

        RowConstraints header = new RowConstraints();
        RowConstraints content = new RowConstraints();
        content.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(header, content);

        stage.setTitle("Chat Server");
        stage.setScene(new Scene(grid, 750, 450));
        stage.show();

        // Start the server (the model) and give it this window as its listener
        int port = Integer.parseInt(Config.load().getProperty("server.port", "3000"));
        server = new ChatServer(port, this);
        try {
            server.start();
        } catch (Exception e) {
            onLog("Could not start server: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (server != null) server.stop();
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> {
            Matcher m = CHAT_LINE.matcher(message);
            if (m.matches()) {
                // Chat line like "[14:32] Alice: hello"
                Text time = new Text(m.group(1) + " ");
                time.setFill(Color.GRAY);

                Text name = new Text(m.group(2));
                name.setFill(UserColors.dark(m.group(2)));
                name.setFont(Font.font("System", FontWeight.BOLD, 12));

                Text body = new Text(": " + m.group(3) + "\n");

                logFlow.getChildren().addAll(time, name, body);
            } else {
                // Any other log line (Server Started, Welcome Bob, ...)
                logFlow.getChildren().add(new Text(message + "\n"));
            }
        });
    }

    @Override
    public void onUsersChanged(List<String> usernames) {
        Platform.runLater(() -> users.setAll(usernames));
    }
}