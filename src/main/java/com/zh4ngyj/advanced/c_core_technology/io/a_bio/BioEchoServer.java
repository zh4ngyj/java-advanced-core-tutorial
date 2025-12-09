package com.zh4ngyj.advanced.c_core_technology.io.a_bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简单阻塞 IO 回显服务。
 * 关键点：accept/read 均为阻塞，连接数与线程数基本 1:1。
 */
public class BioEchoServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "9000"));
        ExecutorService pool = Executors.newFixedThreadPool(
                Integer.parseInt(System.getProperty("threads", "4"))
        );

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("BIO echo server started on port " + port);
            // 阻塞等待新连接
            while (true) {
                Socket socket = serverSocket.accept();
                pool.execute(() -> handleConnection(socket));
            }
        }
    }

    private static void handleConnection(Socket socket) {
        String remote = socket.getRemoteSocketAddress().toString();
        System.out.println("Connected: " + remote);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[" + remote + "] " + line);
                writer.println("echo: " + line);
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IO error with " + remote + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("Closed: " + remote);
        }
    }
}

