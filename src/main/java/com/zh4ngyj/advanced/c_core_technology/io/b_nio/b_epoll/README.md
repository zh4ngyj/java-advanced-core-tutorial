## epoll/kqueue 说明

Java NIO 的 `Selector` 在不同平台会映射到不同实现：
- Linux 通常使用 epoll（`sun.nio.ch.EPollSelectorProvider`）。
- macOS/BSD 使用 kqueue。
- Windows 使用 Select 实现。

本目录提供一个小工具打印当前 JDK 的 SelectorProvider 与 Selector 实际类型，帮助验证 README 中描述的“多路复用”在 OS 层对应何种机制。

### 运行
```
java com.zh4ngyj.advanced.c_core_technology.io.b_nio.b_epoll.EpollSelectorInfo
```

在 Linux 运行时可看到实现类名称中包含 `EPoll`，可与顶层文档的 epoll 优势对照理解。

