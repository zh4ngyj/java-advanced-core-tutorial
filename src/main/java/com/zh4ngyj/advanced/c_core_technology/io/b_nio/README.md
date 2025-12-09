## NIO 演示

本目录包含基于 Selector 的多路复用示例以及零拷贝文件传输示例。

### 结构
- `a_selectOrPoll`：Selector 回显服务，展示非阻塞 + 多路复用。
- `b_epoll`：在 Linux/Windows 下打印实际使用的 SelectorProvider/Selector 实现，说明 epoll/kqueue 的差异。
- `c_zero_copy`：使用 `FileChannel#transferTo` 展示零拷贝文件传输。

详细使用方法见各子目录 README。运行示例前确保项目根目录下存在示例文件 `Text.txt`，或通过系统属性覆盖文件路径。

