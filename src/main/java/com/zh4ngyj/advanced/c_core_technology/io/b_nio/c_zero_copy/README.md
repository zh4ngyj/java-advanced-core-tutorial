## 零拷贝示例

演示 `FileChannel#transferTo` 将文件内容直接发送到目标通道，绕过 JVM 堆内存，减少数据拷贝。

### 运行
```
java -Dsrc=Text.txt -Ddst=target/zero-copy-output.txt \
  com.zh4ngyj.advanced.c_core_technology.io.b_nio.c_zero_copy.ZeroCopyFileTransfer
```
- 未指定时默认从项目根 `Text.txt` 拷贝到 `target/zero-copy-output.txt`。
- 可对比传统 `Files.copy`，观察 CPU/内存占用的差异。

### 关联概念
- 顶层 README 中的“硬盘 -> 内核 Buffer -> 网卡”路径依赖内核态拷贝。
- `transferTo/transferFrom` 利用 sendfile 等系统调用减少用户态参与，是 NIO 另一性能杀器。

