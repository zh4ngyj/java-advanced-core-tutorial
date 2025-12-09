## Selector 回显示例

展示“同步非阻塞 + IO 多路复用”：
- 服务端使用 `Selector` 监听 `OP_ACCEPT`/`OP_READ`。
- SocketChannel 配置为非阻塞，无需 1:1 线程。
- 客户端同样使用 NIO 发送消息并读取回显。

### 运行
1. 启动服务端（默认 9001，可用 `-Dport=9100` 指定）：
   ```
   java com.zh4ngyj.advanced.c_core_technology.io.b_nio.a_selectOrPoll.NioSelectorEchoServer
   ```
2. 启动客户端，输入内容回车；输入 `quit` 结束：
   ```
   java com.zh4ngyj.advanced.c_core_technology.io.b_nio.a_selectOrPoll.NioSelectorEchoClient
   ```

### 观察
- 服务端只有一个主循环线程，随着连接增多不会创建新线程。
- 当 `select()` 返回后才对就绪的 fd 调用 `read()`，对照顶层 README 的“阶段二需用户线程完成拷贝”。

