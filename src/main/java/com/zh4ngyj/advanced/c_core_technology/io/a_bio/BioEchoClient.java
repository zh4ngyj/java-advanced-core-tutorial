package com.zh4ngyj.advanced.c_core_technology.io.a_bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 阻塞 IO 客户端：从控制台读取一行，发送到服务端，再阻塞等待回显。
 */
public class BioEchoClient {

    public static void main(String[] args) throws IOException {
        String host = System.getProperty("host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("port", "9000"));

        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("BIO client connected to " + host + ":" + port);
            System.out.println("Type anything and press Enter (quit to exit)");

            while (true) {
                String line = scanner.nextLine();
                writer.println(line);
                String resp = reader.readLine(); // 阻塞等待回显
                System.out.println("server -> " + resp);
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
            }
        }
    }
}

