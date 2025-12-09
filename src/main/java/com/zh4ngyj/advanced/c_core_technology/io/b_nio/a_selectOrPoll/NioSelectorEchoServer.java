package com.zh4ngyj.advanced.c_core_technology.io.b_nio.a_selectOrPoll;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 基于 Selector 的非阻塞回显服务。
 * select() 是唯一阻塞点；真正的数据拷贝仍由用户线程完成。
 */
public class NioSelectorEchoServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "9001"));

        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()) {

            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO selector echo server started on port " + port);

            while (true) {
                selector.select(); // 阻塞等待就绪事件
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    try {
                        if (key.isAcceptable()) {
                            handleAccept(selector, server);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        key.cancel();
                        closeQuietly(key.channel());
                    }
                }
            }
        }
    }

    private static void handleAccept(Selector selector, ServerSocketChannel server) throws IOException {
        SocketChannel channel = server.accept();
        if (channel == null) {
            return;
        }
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.register(selector, SelectionKey.OP_READ, buffer);
        System.out.println("Accepted: " + channel.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        int read = channel.read(buffer);
        if (read == -1) {
            closeQuietly(channel);
            key.cancel();
            return;
        }
        if (read > 0) {
            buffer.flip();
            channel.write(buffer); // 回显
            buffer.compact(); // 保留未消费的数据
        }
    }

    private static void closeQuietly(java.nio.channels.Channel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }
}

