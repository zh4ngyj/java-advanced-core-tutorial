package com.zh4ngyj.advanced.c_core_technology.io.b_nio.a_selectOrPoll;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 非阻塞客户端示例：connect/read/write 都采用 NIO Channel。
 */
public class NioSelectorEchoClient {

    public static void main(String[] args) throws IOException {
        String host = System.getProperty("host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("port", "9001"));

        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(host, port));
            channel.configureBlocking(true); // 客户端保持简单，读写阻塞即可
            System.out.println("NIO client connected to " + host + ":" + port);
            System.out.println("Type anything and press Enter (quit to exit)");

            Scanner scanner = new Scanner(System.in);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                String line = scanner.nextLine();
                channel.write(StandardCharsets.UTF_8.encode(line + "\n"));

                buffer.clear();
                int read = channel.read(buffer); // 阻塞等回显
                if (read == -1) {
                    break;
                }
                buffer.flip();
                String resp = StandardCharsets.UTF_8.decode(buffer).toString().trim();
                System.out.println("server -> " + resp);
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
            }
        }
    }
}

