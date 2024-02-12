package org.academiadecodigo.nanderthals;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
