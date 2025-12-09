package com.zh4ngyj.advanced.c_core_technology.io.b_nio.c_zero_copy;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 使用 transferTo 进行文件拷贝的零拷贝演示。
 */
public class ZeroCopyFileTransfer {

    public static void main(String[] args) throws IOException {
        Path src = Path.of(System.getProperty("src", "Text.txt"));
        Path dst = Path.of(System.getProperty("dst", "target/zero-copy-output.txt"));
        Files.createDirectories(dst.getParent());

        long bytes;
        try (FileChannel in = FileChannel.open(src, StandardOpenOption.READ);
             FileChannel out = FileChannel.open(dst, StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bytes = in.transferTo(0, in.size(), out);
        }
        System.out.println("Zero-copy transfer complete, bytes copied: " + bytes);
        System.out.println("from: " + src.toAbsolutePath());
        System.out.println("to:   " + dst.toAbsolutePath());
    }
}

