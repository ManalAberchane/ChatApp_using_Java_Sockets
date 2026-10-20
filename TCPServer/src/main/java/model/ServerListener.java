package model;

import java.util.List;

public interface ServerListener {
    void onLog(String message);
    void onUsersChanged(List<String> usernames);
}