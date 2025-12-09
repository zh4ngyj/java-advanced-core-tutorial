package com.zh4ngyj.advanced.c_core_technology.io.c_aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO 回显服务：read/write 均异步完成，回调在 IO 完成后触发。
 */
public class AioEchoServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(System.getProperty("port", "9002"));
        CountDownLatch latch = new CountDownLatch(1);

        try (AsynchronousServerSocketChannel server =
                     AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port))) {
            System.out.println("AIO echo server started on port " + port);
            server.accept(server, new AcceptHandler(latch));
            latch.await(); // 保持主线程存活
        }
    }

    private static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
        private final CountDownLatch latch;

        AcceptHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void completed(AsynchronousSocketChannel channel, AsynchronousServerSocketChannel server) {
            // 继续接受下一个连接
            server.accept(server, this);
            try {
                System.out.println("Accepted: " + channel.getRemoteAddress());
            } catch (IOException ignored) {
            }
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            // 发起异步读：阶段一+二由 OS/线程池完成，完成后回调
            channel.read(buffer, buffer, new EchoHandler(channel));
        }

        @Override
        public void failed(Throwable exc, AsynchronousServerSocketChannel server) {
            System.err.println("Accept failed: " + exc.getMessage());
            latch.countDown();
        }
    }

    private static class EchoHandler implements CompletionHandler<Integer, ByteBuffer> {
        private final AsynchronousSocketChannel channel;

        EchoHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(Integer read, ByteBuffer buffer) {
            if (read == -1) {
                closeQuietly();
                return;
            }
            buffer.flip();
            channel.write(buffer, buffer, new CompletionHandler<>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    attachment.clear();
                    // 再次异步读取下一条消息
                    channel.read(attachment, attachment, EchoHandler.this);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.err.println("Write failed: " + exc.getMessage());
                    closeQuietly();
                }
            });
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            System.err.println("Read failed: " + exc.getMessage());
            closeQuietly();
        }

        private void closeQuietly() {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }
}

