package com.zh4ngyj.advanced.c_core_technology.io;

import java.io.*;

public class SimpleIODemo {

    public static void main(String[] args) throws Exception {

        // 输入流用于从 Java 应用程序的源中读取数据。数据可以是任何内容，如文件、数组、外围设备或套接字。
        // 在 Java 中，类 java.io.InputStream 是所有 Java IO 输入流的基础类。
        inputStream();

        // 输出流用于将数据（文件、数组、外围设备或套接字）写入目标。
        // 在 Java 中，java.io.OutputStream 类是所有 Java IO 输出流的基础类。
        outputStream();
    }

    public static void inputStream() throws IOException {
        System.out.println("******inputStream******");
        InputStream input = null;
        try {

            input = new FileInputStream("Text.txt");

            // read() method - reading and printing Characters one by one
            System.out.println("Char - " + (char) input.read());
            System.out.println("Char - " + (char) input.read());

            // mark() - read limiting the 'input' input stream
            input.mark(0);

            // skip() - it results in skipping of 'e' in Ge'e'ksforGeeks
            input.skip(1);
            System.out.println("skip() method comes to play");
            System.out.println("mark() method comes to play");
            System.out.println("Char - " + (char) input.read());
            System.out.println("Char - " + (char) input.read());

            boolean check = input.markSupported();
            if (input.markSupported()) {
                // reset() method - repositioning the stream to marked positions.
                input.reset();
                System.out.println("reset() invoked");
                System.out.println("Char - " + (char) input.read());
                System.out.println("Char - " + (char) input.read());
            } else
                System.out.println("reset() method not supported.");

            System.out.println("input.markSupported() supported" + " reset() - " + check);
        } catch (Exception e) {
            // in case of I/O error
            e.printStackTrace();
        } finally {
            if (input != null) {
                // Use of close() - closing the file and releasing resources
                input.close();
            }
        }
    }

    public static void outputStream() throws IOException {
        System.out.println("******outputStream******");
        OutputStream output
                = new FileOutputStream("file.txt");
        byte b[] = {65, 66, 67, 68, 69, 70};

        // illustrating write(byte[] b) method
        output.write(b);

        // illustrating flush() method
        output.flush();

        // illustrating write(int b) method
        for (int i = 71; i < 75; i++) {
            output.write(i);
        }

        output.flush();

        // close the stream
        output.close();


    }

}
