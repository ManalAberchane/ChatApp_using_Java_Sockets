package model;

public interface MessageListener {
    void onMessage(String text);
    void onDisconnected();
}