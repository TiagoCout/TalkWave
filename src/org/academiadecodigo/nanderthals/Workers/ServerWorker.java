package org.academiadecodigo.nanderthals.Workers;

import org.academiadecodigo.nanderthals.Room;
import org.academiadecodigo.nanderthals.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerWorker implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private Server server;
    private Room currentRoom;

    public ServerWorker(Socket clientSocket, Server server, Room room) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.currentRoom = room;
        streams();
    }

    private void streams() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        handleClient();
    }

    public String getUsername() {
        return username;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    private void handleClient() {
        try {
            String clientMessage;
            writer.println("Enter your TalkWave username: ");
            username = reader.readLine();

            currentRoom.sendToRoom(username + " has joined the chat.", this);

            while ((clientMessage = reader.readLine()) != null) {
                if (clientMessage.startsWith("/")) {
                    handleCommand(clientMessage);
                    continue;
                }

                currentRoom.sendToRoom("<" + username + "> " + clientMessage, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            handleDisconnect();
        }
    }

    private void handleDisconnect() {
        currentRoom.removeClient(this);
        server.sendToAll(username + " has left the chat.", this);

        try {
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/list":
                listUsers();
                break;
            case "/whisper":
                whisper(command);
                break;
            // Add more cases as needed for other commands
            default:
                writer.println("Invalid command: " + cmd);
        }
    }

    private void listUsers() {
        List<ServerWorker> clients = new LinkedList<>();

        for (Room room : server.getRoomLinkedList()) {
            clients.addAll(room.getServerWorkerLinkedList());
        }

        for (ServerWorker client : clients) {
            writer.println(client.getUsername() + " is in the room: " + client.getCurrentRoom().getRoomName());
        }
    }

    private void whisper(String message) {
        String[] parts = message.split("\\s+", 3);

        if (parts.length == 3) {
            String targetUsername = parts[1];
            String whisperMessage = parts[2];

            List<ServerWorker> clients = server.getAllServerWorkers();

            synchronized (clients) {
                for (ServerWorker worker : clients) {
                    if (worker.getUsername().equals(targetUsername)) {
                        worker.sendMessage(username + " (whisper) -> " + whisperMessage);
                        return;
                    }
                }
            }

            // If the whisper recipient is not found
            sendMessage("User '" + targetUsername + "' not found.");
        } else {
            // If the whisper command is not in the expected format
            sendMessage("Invalid whisper command. Usage: /whisper <username> <message>");
        }
    }
}
