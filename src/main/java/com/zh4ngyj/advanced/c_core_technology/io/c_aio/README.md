## AIO 演示

基于 `AsynchronousSocketChannel` 的真正异步模型（Proactor 思路）。
- 服务端在发起 read 后立即返回，IO 完成后由操作系统/线程池触发回调。
- 客户端同样使用异步读写，展示无阻塞、无 Selector。

### 运行
1. 启动服务端（默认 9002，可用 `-Dport=9200`）：
   ```
   java com.zh4ngyj.advanced.c_core_technology.io.c_aio.AioEchoServer
   ```
2. 启动客户端，输入内容回车，回显通过回调输出，输入 `quit` 结束：
   ```
   java com.zh4ngyj.advanced.c_core_technology.io.c_aio.AioEchoClient
   ```

### 观察
- 发起 read/write 后线程不阻塞，可继续做其他工作。
- 代码注释标明哪些逻辑对应顶层 README 的“阶段一/阶段二由 OS 完成再通知用户”。

