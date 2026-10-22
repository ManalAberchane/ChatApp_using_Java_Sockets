package view;

import model.Config;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import model.ChatClient;
import model.MessageListener;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientApp extends Application implements MessageListener {
    private static final Pattern CHAT_LINE = Pattern.compile("^(\\[\\d{2}:\\d{2}\\]) ([^:]+): (.*)$");

    private Stage stage;
    private String host;
    private int port;
    private ChatClient client;

    // Chat screen controls
    private TextFlow chatFlow;
    private ScrollPane chatScroll;
    private TextField inputField;
    private Button sendButton;
    private Button leaveButton;
    private Label statusLabel;
    private Circle statusDot;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // Command-line arguments take priority; otherwise use config.properties
        List<String> args = getParameters().getRaw();
        Properties cfg = Config.load();
        host = args.size() >= 2 ? args.get(0) : cfg.getProperty("server.ip", "localhost");
        port = args.size() >= 2
                ? Integer.parseInt(args.get(1))
                : Integer.parseInt(cfg.getProperty("server.port", "3000"));

        stage.setTitle("Chat Client");
        stage.setScene(buildLoginScene());
        stage.show();
    }

    @Override
    public void stop() {
        if (client != null) client.disconnect();
    }

    // ---------------- Login screen ----------------

    private Scene buildLoginScene() {
        Label title = new Label("Join the Chat");
        Label serverInfo = new Label("Server: " + host + ":" + port);
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-prompt-text-fill: gray;");

        Button joinButton = new Button("Join");
        joinButton.setDefaultButton(true);
        joinButton.setDisable(true);

        Button readOnlyButton = new Button("Join read-only");

        usernameField.textProperty().addListener((obs, oldV, newV) ->
                joinButton.setDisable(newV.trim().isEmpty()));

        joinButton.setOnAction(e -> connect(usernameField.getText().trim(), errorLabel));
        readOnlyButton.setOnAction(e -> connect("", errorLabel));

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(24));
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        grid.add(title, 0, 0, 2, 1);
        grid.add(serverInfo, 0, 1, 2, 1);
        grid.add(usernameField, 0, 2, 2, 1);
        grid.add(joinButton, 0, 3);
        grid.add(readOnlyButton, 1, 3);
        grid.add(errorLabel, 0, 4, 2, 1);

        GridPane.setHalignment(title, HPos.CENTER);
        GridPane.setHalignment(serverInfo, HPos.CENTER);
        GridPane.setHalignment(errorLabel, HPos.CENTER);

        return new Scene(grid, 360, 260);
    }

    /** Connects on a background thread so the window never freezes */
    private void connect(String username, Label errorLabel) {
        errorLabel.setText("Connecting...");
        errorLabel.setTextFill(Color.GRAY);

        Thread.ofVirtual().start(() -> {
            ChatClient c = new ChatClient(this);
            try {
                c.connect(host, port, username);
                client = c;
                Platform.runLater(() -> showChat(username));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Could not connect: " + ex.getMessage());
                });
            }
        });
    }

    // ---------------- Chat screen ----------------

    private void showChat(String username) {
        boolean readOnly = username.isEmpty();

        chatFlow = new TextFlow();
        chatFlow.setPadding(new Insets(6));
        chatScroll = new ScrollPane(chatFlow);
        chatScroll.setFitToWidth(true);
        chatFlow.heightProperty().addListener((obs, o, n) -> chatScroll.setVvalue(1.0));

        statusDot = new Circle(6, Color.LIMEGREEN);
        statusLabel = new Label("Online");
        Label modeLabel = new Label(readOnly ? "READ-ONLY MODE" : "Signed in as " + username);
        modeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        leaveButton = new Button("LEAVE");
        leaveButton.setOnAction(e -> client.disconnect());

        inputField = new TextField();
        sendButton = new Button("SEND");
        sendButton.setDefaultButton(true);

        if (readOnly) {
            inputField.setPromptText("You are in READ-ONLY MODE");
            inputField.setDisable(true);
            sendButton.setDisable(true);
        } else {
            inputField.setPromptText("Type a message (allUsers, bye)...");
            inputField.setStyle("-fx-prompt-text-fill: gray;");
            inputField.setOnAction(e -> sendMessage());
            sendButton.setOnAction(e -> sendMessage());
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(12));
        grid.setHgap(8);
        grid.setVgap(8);

        // Row 0: status bar
        grid.add(statusDot, 0, 0);
        grid.add(statusLabel, 1, 0);
        grid.add(modeLabel, 2, 0);
        grid.add(leaveButton, 3, 0);
        GridPane.setHalignment(leaveButton, HPos.RIGHT);

        // Row 1: chat area, spanning all 4 columns
        grid.add(chatScroll, 0, 1, 4, 1);

        // Row 2: input and SEND
        grid.add(inputField, 0, 2, 3, 1);
        grid.add(sendButton, 3, 2);

        ColumnConstraints c0 = new ColumnConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints();
        grid.getColumnConstraints().addAll(c0, c1, c2, c3);

        RowConstraints r0 = new RowConstraints();
        RowConstraints r1 = new RowConstraints();
        r1.setVgrow(Priority.ALWAYS);
        RowConstraints r2 = new RowConstraints();
        grid.getRowConstraints().addAll(r0, r1, r2);

        stage.setTitle("Chat Client - " + (readOnly ? "Read-only" : username));
        stage.setScene(new Scene(grid, 520, 480));
        if (!readOnly) inputField.requestFocus();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        inputField.clear();

        if (text.equalsIgnoreCase("end") || text.equalsIgnoreCase("bye")) {
            client.disconnect();
        } else {
            client.send(text);
        }
    }

    // ---------------- MessageListener (called from background threads) ----------------

    @Override
    public void onMessage(String line) {
        Platform.runLater(() -> {
            if (chatFlow == null) return;
            Matcher m = CHAT_LINE.matcher(line);
            if (m.matches()) {
                Text time = new Text(m.group(1) + " ");
                time.setFill(Color.GRAY);

                Text name = new Text(m.group(2));
                name.setFill(UserColors.dark(m.group(2)));
                name.setFont(Font.font("System", FontWeight.BOLD, 12));

                Text body = new Text(": " + m.group(3) + "\n");
                chatFlow.getChildren().addAll(time, name, body);
            } else {
                // Server notices such as "[Server] Bob joined the chat"
                Text notice = new Text(line + "\n");
                notice.setFill(Color.DIMGRAY);
                notice.setFont(Font.font("System", FontPosture.ITALIC, 12));
                chatFlow.getChildren().add(notice);
            }
        });
    }

    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            if (statusLabel == null) return;
            statusLabel.setText("Offline");
            statusDot.setFill(Color.RED);
            inputField.setDisable(true);
            sendButton.setDisable(true);
            leaveButton.setDisable(true);
        });
    }
}