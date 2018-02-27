/* @file     multithread_server.java
 * @author   huynguyen
 * @desc     Java socket server, handles multiple clients using threads.
 *
 * Copyright (c) 2018, Distributed Embedded Systems (CCS Labs)
 * All rights reserved.
 */

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class Multithread_Server {

    public static void main(String[] args) throws Exception {

        ServerSocket listener = new ServerSocket(5001);

        try {
            System.out.println("Waiting for incoming connections...");
            while (true) {
                new ConnectionHandler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ConnectionHandler extends Thread {
        private Socket socket;
        private byte[] buffer = new byte[100];

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                System.out.printf("Thread No.: %d\n", Thread.currentThread().getId());

                OutputStream outputStream = socket.getOutputStream();
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(outputStream)),
                        true);
                out.println("Hello from server");

                InputStream inputStream = socket.getInputStream();

                //Wait for data from clients
                while (true) {
                    int bytesRead = inputStream.read(buffer, 0, 100);
                    if (bytesRead < 0) {
                        break;
                    }

                    //Handle the data
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100);
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    String message = byteArrayOutputStream.toString("UTF-8");
                    System.out.printf("Client: %s\n", message);

                    //Message echo
                    out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                    System.out.println("Connection closed!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
