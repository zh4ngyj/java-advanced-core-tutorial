package com.zh4ngyj.advanced.a_basic.exception;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author: zh4ngyj
 * @date: 2025/11/19 9:37
 * @des:
 */
public class ExceptionDemo implements AutoCloseable {

    public static void readFile(String path) throws IOException {
        if (path == null) {
            // 主动抛出运行时异常（unchecked），不需要写在 throws 里
            throw new IllegalArgumentException("路径不能为空");
        }
        try (FileInputStream fis = new FileInputStream(path)) {
            int data = fis.read();
            System.out.println("读到一个字节：" + data);
        } // 自动关闭资源，无需 finally
    }

    public static void main(String[] args) {
        try {
            readFile(null);
        } catch (IllegalArgumentException e) { // catch 运行时异常
            System.out.println("参数错误：" + e.getMessage());
        } catch (IOException e) { // catch 受检异常
            System.out.println("IO 错误：" + e.getMessage());
        } finally {
            System.out.println("程序结束前必定会执行的清理逻辑");
        }

        try (ExceptionDemo demo = new ExceptionDemo()) {
            System.out.println("hello, try-with-resources!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        System.out.println(
"""
`try-with-resources` (Java 7+):
一种语法糖，用于自动关闭实现了 `java.lang.AutoCloseable` 或 `java.io.Closeable` 接口的资源。
""");
    }
}
