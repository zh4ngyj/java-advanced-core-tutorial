package com.zh4ngyj.advanced.c_core_technology.io.c_aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * AIO 客户端：发送数据后不阻塞，回显通过回调打印。
 */
public class AioEchoClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        String host = System.getProperty("host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("port", "9002"));
        CountDownLatch latch = new CountDownLatch(1);

        try (AsynchronousSocketChannel channel = AsynchronousSocketChannel.open()) {
            channel.connect(new InetSocketAddress(host, port)).get();
            System.out.println("AIO client connected to " + host + ":" + port);
            System.out.println("Type anything and press Enter (quit to exit)");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                ByteBuffer buffer = StandardCharsets.UTF_8.encode(line + "\n");
                channel.write(buffer, buffer, new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        ByteBuffer respBuf = ByteBuffer.allocate(1024);
                        channel.read(respBuf, respBuf, new CompletionHandler<>() {
                            @Override
                            public void completed(Integer len, ByteBuffer buf) {
                                buf.flip();
                                System.out.println("server -> " + StandardCharsets.UTF_8.decode(buf).toString().trim());
                                buf.clear();
                                latch.countDown();
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer buf) {
                                System.err.println("Read failed: " + exc.getMessage());
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        System.err.println("Write failed: " + exc.getMessage());
                        latch.countDown();
                    }
                });
                latch.await();
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
                // 为下一轮创建新的 latch
                latch = new CountDownLatch(1);
            }
        } catch (Exception e) {
            throw new IOException("AIO client error", e);
        }
    }
}

