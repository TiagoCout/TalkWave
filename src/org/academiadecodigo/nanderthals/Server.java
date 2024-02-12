package org.academiadecodigo.nanderthals;

import org.academiadecodigo.nanderthals.Workers.ServerWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final LinkedList<Room> roomLinkedList;
    private final ExecutorService fixedPool = Executors.newCachedThreadPool();

    public Server() {
        this.roomLinkedList = new LinkedList<>();
    }

    public void start() throws IOException {
        int portNumber = getPortNumber();

        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("## TalkWave server started on port " + portNumber + " ##");

        Room mainRoom = createMainRoom();
        roomLinkedList.add(mainRoom);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("## NEW CLIENT CONNECTED ##");

            ServerWorker serverWorker = new ServerWorker(clientSocket, this, mainRoom);
            mainRoom.addClient(serverWorker);

            fixedPool.submit(serverWorker);
        }
    }

    public void sendToAll(String message, ServerWorker client) {
        synchronized (roomLinkedList) {
            for (Room room : roomLinkedList) {
                synchronized (room.getServerWorkerLinkedList()) {
                    for (ServerWorker serverWorker : room.getServerWorkerLinkedList()) {
                        if (serverWorker == client) continue;
                        serverWorker.sendMessage(message);
                    }
                }
            }
        }
    }

    private int getPortNumber() throws IOException {
        System.out.println("Select a port");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return Integer.parseInt(reader.readLine());
    }

    public List<ServerWorker> getAllServerWorkers() {
        List<ServerWorker> allServerWorkers = new LinkedList<>();

        for (Room room : roomLinkedList) {
            allServerWorkers.addAll(room.getServerWorkerLinkedList());
        }

        return allServerWorkers;
    }

    public LinkedList<Room> getRoomLinkedList() {
        return roomLinkedList;
    }

    private Room createMainRoom() {
        Room mainRoom = new Room("WaveRoom");
        mainRoom.start();
        return mainRoom;
    }


}
