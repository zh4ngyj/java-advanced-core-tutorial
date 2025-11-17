## 模块: 线程池 (ThreadPool)

线程池是并发编程中的核心组件，用于管理和复用线程，以降低资源消耗、提高响应速度和增强线程的可管理性。<p>
Pooling is the grouping together of resources (assets, equipment, personnel, effort, etc.) 
for the purposes of maximizing advantage or minimizing risk to the users. The term is used in finance, computing and equipment management.——wikipedia

### 为什么需要线程池？
1.  **降低资源消耗**: 通过复用已创建的线程，减少了线程创建和销毁的开销。
2.  **提高响应速度**: 当任务到达时，可以直接使用池中已有的线程，无需等待新线程创建。
3.  **提高线程的可管理性**: 线程是稀缺资源，如果无限制地创建，不仅会消耗系统资源，还会降低系统稳定性。线程池可以进行统一的分配、调优和监控。

---

### `ThreadPoolExecutor` 核心参数

手动创建 `ThreadPoolExecutor` 是推荐的最佳实践，需要理解其七大核心参数：

1.  **`corePoolSize` (核心线程数)**
    - 线程池中保持存活的核心线程数量，即使它们是空闲的。

2.  **`maximumPoolSize` (最大线程数)**
    - 线程池能够容纳同时执行的最大线程数。当工作队列满了之后，才会创建新线程，直到达到此上限。

3.  **`keepAliveTime` (空闲线程存活时间)**
    - 当线程池中的线程数量大于 `corePoolSize` 时，如果一个线程空闲时间达到 `keepAliveTime`，它将被终止。

4.  **`unit` (时间单位)**
    - `keepAliveTime` 的时间单位（如 `TimeUnit.SECONDS`）。

5.  **`workQueue` (工作队列)**
    - 用于保存等待执行的任务的阻塞队列。常见的队列有：
        - `LinkedBlockingQueue`: 无界队列（默认容量为 `Integer.MAX_VALUE`），可能导致OOM。
        - `ArrayBlockingQueue`: 有界队列，需要指定容量。
        - `SynchronousQueue`: 不存储元素的队列，每个插入操作必须等待一个移除操作。

6.  **`threadFactory` (线程工厂)**
    - 用于创建新线程。可以自定义线程工厂，为线程设置有意义的名称，方便调试。

7.  **`handler` (拒绝策略)**
    - 当队列和线程池都满了之后，新任务的拒绝策略。
        - `AbortPolicy` (默认): 抛出 `RejectedExecutionException` 异常。
        - `CallerRunsPolicy`: 由提交任务的线程自己来执行该任务。
        - `DiscardPolicy`: 直接丢弃任务，不抛出异常。
        - `DiscardOldestPolicy`: 丢弃队列中最老的任务，然后尝试重新提交当前任务。

---

### 线程池工作流程
1.  当一个新任务提交时，线程池判断 `corePoolSize` 是否已满。如果未满，则创建新线程执行任务。
2.  如果核心线程数已满，则判断工作队列 `workQueue` 是否已满。如果未满，则将任务放入队列等待。
3.  如果工作队列已满，则判断 `maximumPoolSize` 是否已满。如果未满，则创建新的非核心线程来执行任务。
4.  如果最大线程数也已满，则执行拒绝策略 `handler`。

~~~mermaid
flowchart TB
    A[用户] --> |任务提交| B[任务分配]
    B --> |响应| A
    
    B --> |缓冲执行| C["任务缓冲：<br/>阻塞队列Task|Task|Task|Task"]
    B --> |直接执行| D((Task))
    B --> E[任务拒绝]
    
    F[任务获取] --> C
    F --> G[线程执行任务]
    H[线程分配] --> G
    G --> F
    D --> H
    
    H --> I["线程池<br/>corePool<br/>Thread Thread<br/>Thread Thread<br/>Thread Thread<br/>Thread Thread<br/>maximumPool"]
    I --> J[线程回收]
~~~

---

### 线程池状态
- **RUNNING**: 能接受新任务，并能处理阻塞队列中的任务。
- **SHUTDOWN**: 不接受新任务，但会处理阻塞队列中的任务。调用 `shutdown()` 方法后进入此状态。
- **STOP**: 不接受新任务，不处理阻塞队列中的任务，并中断正在执行任务的线程。调用 `shutdownNow()` 方法后进入此状态。
- **TIDYING**: 所有任务都已终止，`workerCount` 为0，线程池会进入该状态并准备调用 `terminated()` 方法。
- **TERMINATED**: `terminated()` 方法执行完毕。

生命周期
~~~mermaid
flowchart LR
    A[RUNNING] --> |"shutdown()"| B[SHUTDOWN]
    A[RUNNING] --> |"shutdownNow()"| C[STOP]
    
    B --> |"阻塞队列为空，线程池中工作线程数量为0"| D[TIDYING]
    C --> |"线程池中工作线程数量为0"| D[TIDYING]
    
    D --> |"terminated()"| E[TERMINATED]
~~~

本包中的示例 (`ThreadPoolExecutorDemo.java`) 展示了如何手动创建线程池并观察其工作行为。

### 任务调度流程

1. 首先检测线程池运行状态，如果不是RUNNING，则直接拒绝，线程池要保证在RUNNING的状态下执行任务。
2. 如果workerCount < corePoolSize，则创建并启动一个线程来执行新提交的任务。
3. 如果workerCount >= corePoolSize，且线程池内的阻塞队列未满，则将任务添加到该阻塞队列中。
4. 如果workerCount >= corePoolSize && workerCount < maximumPoolSize，且线程池内的阻塞队列已满，则创建并启动一个线程来执行新提交的任务。
5. 如果workerCount >= maximumPoolSize，并且线程池内的阻塞队列已满, 则根据拒绝策略来处理该任务, 默认的处理方式是直接抛异常。

~~~mermaid
flowchart TB
    A[开始] --> B[提交任务]
    B --> C{线程池是否还在运行}
    C --> |是| D{工作线程数>核心数?}
    D --> |否| E{阻塞队列是否已满}
    E --> |是| F{工作线程数<最大线程数}

    C --> |否| G[任务拒绝]
    D --> |是| H[添加工作线程并执行] 
    E --> |否| I[添加任务到阻塞队列]
    F --> |是| J[添加工作线程并执行]
    F --> |否| G

    H --> Z[结束]
    I --> Z
    J --> Z
    G --> Z
~~~

### 任务缓冲
线程池通过阻塞队列（BlockingQueue）对任务进行缓冲，常见的工作队列有以下几种：
1. **`ArrayBlockingQueue`**:一个又数组实现的有界阻塞队列，按FIFO（先进先出）原则对元素进行排序。支持公平锁和非公平锁。
2. **`LinkedBlockingQueue`**:一个由链表结构组成的有界阻塞队列，按FIFO的原则对元素进行排序。此队列默认长度伟Integer.MAX_VALUE,所以默认创建的队列有容量危险。
3. **`PriorityBlockingQueue`**:一个支持线程优先级排序的无界队列，默认自然序进行排序，
4. **`DelayQueue`**:一个实现PriorityQueue的延迟获取的无界队列，在创建元素时，可以指定多久才能从队列中获取当前元素。只有延时期满后才能从队列中获取元素。
5. **`SynchronousQueue`**:一个不存在元素的阻塞队列，每一个put操作必须等待take操作，否则不能添加元素。支持公平锁和非公平锁。 
   Executors.newCachedThreadPool()就使用了SyschronousQueue，这个线程池根据需要创建新的线程，如果有空闲线程则重复使用，线程空闲了60秒后就会被回收。
6. **`LinkedTransferQueue`**:一个有链表结构组成的无界阻塞队列，相当于其它队列，LinkedTransferQueue多了transfer和trtTransfer方法，类似于SynchronousQueue的功能。
7. **`LinkedBlockingDeque`**:一个由链表结构组成的双向阻塞队列。队列头部和尾部都可以添加和移除元素，降低了锁的竞争。

## 参考资料
> https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html