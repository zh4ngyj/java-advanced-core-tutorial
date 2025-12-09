##  Java IO 模型

### 一. 前置知识
例如一次网络 IO 读取操作，包含两个阶段:
1. 等待数据准备(Waiting for the data to be ready)
   - 数据从**网卡**通过DMA（直接内存访问）拷贝到**内核缓冲区(Kernel Buffer/Socket Buffer)**。
   - 如果数据还没有到，或者没有收满，就是等待阶段。 
2. 将数据从内核拷贝到用户空间(Copying the data from the kernel to the process)
   - CPU 将数据从**内核缓冲区**拷贝到**用户缓冲区(User buffer/JVM Heap)**

所有的 IO 模型优化，本质上都是在优化这两个阶段的等待方式和 CPU 利用率。

### 二. Java BIO(Blocking IO) 传统的阻塞模型
BIO是JDK 1.4之前的唯一选择。

1. Java 层面
`InputStream.read()` 或者 `ServerSocket.accept()`
2. 操作系统底层（Linux） 
   - 当 Java 线程调用 read() 时，会触发系统调用（如 recvfrom）。 
   阶段一（等待数据）：此时内核中没有数据。操作系统会将该 Java 线程从 CPU 运行队列中移除，
   状态置为 睡眠（TASK_INTERRUPTIBLE）。这个线程彻底挂起，不消耗 CPU，但占用内存资源（线程栈）。
   - 阶段二（数据拷贝）： 当网卡接收到数据，网卡中断信号唤醒 CPU，CPU 将数据拷入内核。
   内核唤醒该线程，将其放回运行队列。线程醒来，CPU 再次介入，将数据从内核拷贝到 Java 堆内存。
3. 痛点分析
   线程就是成本： 一个连接需要一个线程。如果有 10,000 个连接，绝大多数连接是空闲的（Keep-Alive），但你必须维护 10,000 个线程。
   上下文切换 (Context Switch)： 线程在“运行”和“阻塞”之间频繁切换，涉及到用户态与内核态的切换，保存寄存器、栈指针等，开销巨大。

### 三. Java NIO (Non-blocking IO) - 多路复用模型
JDK 1.4 引入。这是目前高性能网络框架（Netty, Tomcat, Jetty）的基石。
注意： Java 的 NIO 在操作系统层面对应的是 **IO 多路复用 (IO Multiplexing) 模型**。

1. Java 层面
   核心组件：Channel（通道）、Buffer（缓冲区）、Selector（选择器）。

2. 操作系统底层 (Linux)
   NIO 的核心在于 Selector，它利用了操作系统的高级 IO 函数：select / poll (早期) 或 epoll (现代 Linux)。

非阻塞模式： Java 将 Socket 设置为非阻塞 (O_NONBLOCK)。如果调用 read 没数据，内核直接返回错误（EWOULDBLOCK），而不是挂起线程。
多路复用 (Multiplexing)：
Java 线程不直接守着 socket 睡觉。
Java 线程把 10,000 个 Socket (文件描述符 FD) 注册给操作系统的 epoll 监听器。
阶段一（等待）： 只有一个线程（Selector 线程）调用 epoll_wait 阻塞。此时内核替你看着这 10,000 个 FD。只要有一个 FD 数据到了，内核通过回调机制迅速激活该 FD，并唤醒 Selector 线程。
阶段二（拷贝）： Selector 线程醒来，拿到“这就绪的几个 FD”，然后在用户态循环处理这几个 FD，调用 read 把数据从内核拷到用户态。
3. 为什么 Epoll 比 Select 强？(NIO 的核心性能点)
   Select/Poll (老旧): 每次调用都要把 10,000 个 FD 从用户态拷到内核态，内核还要遍历这 10,000 个 FD 检查谁有数据。O(N) 复杂度。
   Epoll (先进):
   它在内核维护了一棵红黑树来存所有监听的 FD（不需要每次都拷 FD）。
   它利用网卡中断回调。当数据来时，直接把就绪的 FD 加入到一个“就绪链表”中。
   epoll_wait 实际上只是在检查这个链表是不是空的。O(1) 复杂度。哪怕你有 100 万连接，只要只有 1 个活跃，效率依然极高。
4. 零拷贝 (Zero Copy) - NIO 的另一大杀器
   NIO 提供了 DirectByteBuffer 和 FileChannel.transferTo。

传统 IO： 硬盘 -> 内核 Buffer -> 用户 Buffer -> 内核 Socket Buffer -> 网卡 (4次拷贝，4次上下文切换)。
NIO (mmap/sendfile)： 利用操作系统的 sendfile 指令。数据直接从 硬盘 -> 内核 Buffer -> 网卡。数据根本不经过 JVM 内存，极大降低了 CPU 压力和 GC 压力。

### 四. Java AIO (Asynchronous IO) - 真正的异步
JDK 1.7 引入 (NIO.2)。

1. 区别于 NIO
   NIO 是“同步非阻塞”： Selector 告诉你“数据到了（阶段一完成）”，你自己要去读（阶段二由你完成）。
   AIO 是“异步非阻塞”： 你告诉操作系统：“把数据读到这个 Buffer 里，读完了叫我”。操作系统替你把阶段一和阶段二全做完了，才通知你。
2. 操作系统底层
   - Windows (IOCP): AIO 在 Windows 上表现完美。Windows 的 IO 模型主要就是基于 IOCP (Input/Output Completion Port) 设计的，是真正的操作系统级别的异步。
   - Linux: 这里比较尴尬。Linux 长期以来没有真正的 AIO。
     - 早期 JDK 的 AIO 在 Linux 上是用 Epoll 模拟的（实际上还是 NIO，只是封装了一层回调）。
     - io_uring (新星): 最近 Linux 5.1+ 内核引入了 io_uring，这是真正的异步 IO 接口。高版本的 Java (Project Loom / Newer JDKs) 正在尝试适配 io_uring，一旦普及，Linux 下的 Java AIO 性能将会有质的飞跃。

### 五. 总结：从 OS 视角看三者
|特性	| BIO	| NIO (Reactor模式) |	AIO (Proactor模式)|
| ---- | ---- | ---- | ---- |
|Java 对象|	Socket / InputStream|	SocketChannel / Selector	|AsynchronousSocketChannel|
|阻塞点|	read() 期间全程阻塞|	仅 select() 时阻塞，read() 时同步拷贝数据|	发起读请求后立即返回，完全不阻塞|
|OS 对应机制|	recv (Blocking)	|epoll / kqueue (Multiplexing)|	Windows: IOCP <br> Linux: epoll(模拟) / io_uring|
|谁负责拷贝数据|	用户线程 (等待+拷贝)|	用户线程 (仅拷贝)|	操作系统 (拷贝完通知用户)|
|适用场景|	连接少且长 (如数据库连接)|	连接多且短/长 (聊天室、HTTP服务器)|	连接多且长 (重IO操作)|

一句话总结：
- **BIO：** 是你自己在柜台排队取餐。
- **NIO：** 是你拿了号（注册 Selector），在座位上玩手机，广播叫到你号了（事件就绪），你再去柜台取餐（自己 read）。
- **AIO：** 是你点了外卖，外卖员直接把饭送到你桌子上（OS 帮你 read 完），然后拍拍你肩膀叫你吃。

