package org.academiadecodigo.nanderthals;

import org.academiadecodigo.nanderthals.Workers.ServerWorker;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Room {

    private final String roomName;
    private final List<ServerWorker> serverWorkerLinkedList;
    private Thread roomThread;

    public Room(String roomName) {
        this.roomName = roomName;
        this.serverWorkerLinkedList = Collections.synchronizedList(new LinkedList<>());
    }

    public String getRoomName() {
        return roomName;
    }

    public List<ServerWorker> getServerWorkerLinkedList() {
        return serverWorkerLinkedList;
    }

    public List<ServerWorker> getAllServerWorkers() {
        return new LinkedList<>(serverWorkerLinkedList);
    }

    public void addClient(ServerWorker serverWorker) {
        synchronized (serverWorkerLinkedList) {
            serverWorkerLinkedList.add(serverWorker);
        }
    }

    public void removeClient(ServerWorker serverWorker) {
        synchronized (serverWorkerLinkedList) {
            serverWorkerLinkedList.remove(serverWorker);
        }
    }

    public void sendToRoom(String message, ServerWorker client) {
        synchronized (serverWorkerLinkedList) {
            for (ServerWorker serverWorker : serverWorkerLinkedList) {
                if (serverWorker == client) continue;
                serverWorker.sendMessage(message);
            }
        }
    }

    public void start() {
        roomThread = new Thread(() -> {
            while (true) {
                // Lógica específica da sala (se necessário)
                try {
                    Thread.sleep(1000); // Exemplo: Dormir por 1 segundo, ajustar conforme necessário
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        roomThread.start();
    }

    public void stop() {
        if (roomThread != null && roomThread.isAlive()) {
            roomThread.interrupt();
        }
    }
}
