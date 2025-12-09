package com.zh4ngyj.advanced.c_core_technology.io.b_nio.b_epoll;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * 打印当前平台下 Selector 的具体实现，便于对照 epoll/select/kqueue。
 */
public class EpollSelectorInfo {

    public static void main(String[] args) throws IOException {
        SelectorProvider provider = SelectorProvider.provider();
        try (Selector selector = provider.openSelector()) {
            System.out.println("OS: " + System.getProperty("os.name") +
                    " " + System.getProperty("os.version"));
            System.out.println("SelectorProvider: " + provider.getClass().getName());
            System.out.println("Selector impl:     " + selector.getClass().getName());
            System.out.println("Note: On Linux, EPollSelectorProvider/EPollSelector" +
                    " indicates epoll is used under the hood.");
        }
    }
}

