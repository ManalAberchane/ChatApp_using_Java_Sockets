# Multi-Client Chat Application (Java Sockets + JavaFX)

Mini Project 1: a group chat application where multiple clients exchange
messages in real time through a central server, using TCP sockets.

**Author:** [Manal Aberchane]
**Course:** [CSC 3374 - Adv.Distribut.Programming Paradigms], [Professor El Habib Nfaoui]

## Features

### Client
- Username login screen before entering the chat
- **Read-only mode**: joining without a username lets you read but not send
- Real-time messaging via the SEND button or the Enter key
- `allUsers` command returns the list of active users (to the requester only)
- Disconnect by typing `end` or `bye`, or with the LEAVE button
- "Online/Offline" label and a colored status circle
- Usernames displayed in color in the chat

### Server
- Accepts multiple concurrent clients (one **virtual thread** per client)
- Formats each message with the sender's name and the time, then broadcasts it
- Live `ListView` of connected users, each with a colored background
- Activity log ("Server Started", "Waiting for Client", "Welcome Alice", chat messages)

## Architecture

Both projects separate the **model** (sockets and logic, no JavaFX) from the
**view** (JavaFX). The model reports events through listener interfaces
(`ServerListener`, `MessageListener`), which the JavaFX windows implement.

```
TCPServer/src/main/java
  TCPServer.java        launcher (main class)
  model/                ChatServer, ClientHandler, ServerListener, Config
  view/                 ServerApp, UserColors

TCPClient/src/main/java
  TCPClient.java        launcher (main class)
  model/                ChatClient, MessageListener, Config
  view/                 ClientApp, UserColors
```

Diagrams are in the [`docs`](docs) folder:
- Class diagram: *(coming soon)*
- Deployment diagram: *(coming soon)*

## Communication protocol

One text line per message over TCP.

| Situation | Behavior |
|---|---|
| Client connects | First line sent is the username. An empty line means read-only mode |
| Normal line | Server broadcasts `[HH:mm] username: text` to all clients |
| `allUsers` | Server replies only to the requesting client |
| `end` / `bye` | Client is removed and the others are notified |
| Read-only client sends text | Rejected by the **server** (not only by the UI) |

Read-only users are named `Guest-1`, `Guest-2`, and so on.

## Requirements

- JDK 21 or newer (virtual threads)
- Maven (bundled with IntelliJ IDEA)

## Configuration

Both apps read a `config.properties` file from `src/main/resources`.

**Server** (`TCPServer/src/main/resources/config.properties`)
```properties
server.port=3000
```

**Client** (`TCPClient/src/main/resources/config.properties`)
```properties
server.ip=localhost
server.port=3000
```

Command-line arguments given to the client take priority over the file.

## How to run

### From the executable JARs (`artifacts/` folder)

Start the server:
```
java -jar TCPServer.jar
```

Start one or more clients:
```
java -jar TCPClient.jar localhost 3000
```

Running the client with no arguments uses the values from `config.properties`.

> The JARs were built on **[Windows 11]**. Because they bundle
> JavaFX for that operating system, build your own JARs if you use a different one.

### From source

```
cd TCPServer
mvn clean package
java -jar target/TCPServer.jar

cd ../TCPClient
mvn clean package
java -jar target/TCPClient.jar localhost 3000
```

In IntelliJ, run the `TCPServer` and `TCPClient` classes directly. Set the
client's program arguments to `localhost 3000`.

## Design notes

- **Colors:** each username's color is derived from a hash of the name, so it
  looks random across users but the same name gets the same color in the server
  list and in every client window.
- **Launcher classes:** `TCPServer` and `TCPClient` do not extend
  `Application`. They call `Application.launch(...)`, which avoids the
  "JavaFX runtime components are missing" error in the JARs.

## Demo video

[Link coming soon]

## Technical post (optional)

[Link coming soon]