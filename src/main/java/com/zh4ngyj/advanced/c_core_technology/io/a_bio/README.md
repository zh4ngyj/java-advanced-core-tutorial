## BIO 演示

本示例展示阻塞 IO 的“一个连接一个线程”模型。

### 关键点
- `ServerSocket.accept()` 与 `InputStream.read()` 均为阻塞调用。
- 使用线程池处理每个连接，模拟传统的阻塞式服务端。
- 客户端使用阻塞 `Socket`，逐行写入并等待回显。

### 运行方式
1. 先启动服务端（默认端口 9000，可通过 `-Dport=9001` 指定）：
   ```
   java com.zh4ngyj.advanced.c_core_technology.io.a_bio.BioEchoServer
   ```
2. 再启动客户端，键入任意内容回车即可看到回显，输入 `quit` 退出：
   ```
   java com.zh4ngyj.advanced.c_core_technology.io.a_bio.BioEchoClient
   ```

### 观察点
- 当客户端数量增加时，服务端线程数随之线性增长。
- 查看代码中的注释可对比 README 顶层文档所述的“等待阶段 + 拷贝阶段”。

