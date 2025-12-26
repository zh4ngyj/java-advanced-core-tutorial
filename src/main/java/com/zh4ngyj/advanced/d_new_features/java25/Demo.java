package com.zh4ngyj.advanced.d_new_features.java25;

import java.net.http.HttpClient;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import module java.base;

import static java.util.Calendar.MONDAY;

public class Demo {

    public static void supportJudgeLocalVariableType() {
        System.out.println("Java 25 support keyword: var");
        // Java 8:
        Map<String, List<String>> map = new HashMap<>();

        // Java 25:
        var newMap = new HashMap<String,String>();
        newMap.put("java","var");
        System.out.println(newMap);
        System.out.println("********************java 25********************");
    }

    public static void supportTextBlocks() {
        System.out.println("java 25 support text blocks");
        var json = """
                {
                    "name": "Java 25",
                    "type": "LTS"
                }
                """;
        System.out.println(json);
        System.out.println("********************java 25********************");
    }

    public static void supportSwitchArrowArrowExpression() {
        System.out.println("java 25 supportSwitchArrowArrowExpression");
        var day = MONDAY;
        var result = switch (day) {
            case MONDAY -> 1;
            default -> 0;
        };
        System.out.println(result);
        System.out.println("********************java 25********************");
    }

    public static void supportFinalTypeRecord() {
        System.out.println("使用record定义一个不可变的数据携带类仅需一行。");
        System.out.println("自动生成构造器、getter、equals、hashCode 和 toString。");

        System.out.println("example: record ProgramLanguage(String name, String version) {}");
        record ProgramLanguage(String name, String version) {}
        ProgramLanguage java = new ProgramLanguage("Java", "25");
        System.out.println(java);
    }

    sealed class Parent permits Child1 {
        private String name;
        private String version;
        public Parent(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }
    non-sealed class Child1 extends Parent {
        private String function;
        public Child1(String name, String version) {
            super(name, version);
        }
    }
    public static void supportSealedClass(){
        System.out.println("Java 25 supportSealedClass:");
        System.out.println("non-sealed：解除密封限制，允许任意类继承；");
        System.out.println("sealed：继续限制继承（需再用 permits 指定子类）。");
        System.out.println("********************java 25********************");
    }

    public static Object getObject() {
        return 1;
    }
    public static void supportPatternMatch() {
        System.out.println("支持模式匹配");
        System.out.println("1.instance of:");
        interface Interface{void say();}
        class ImpA implements Interface{
            @Override
            public void say() {
                System.out.println(ImpA.class.getSimpleName());
            }
        }
        class ImpB implements Interface{
            @Override
            public void say() {
                System.out.println(ImpB.class.getSimpleName());
            }
        }
        Interface instance = new ImpA();
        if (instance instanceof ImpB impb) {
            impb.say();
        } else  {
            System.out.println("instance not instanceof ImpB");
        }
        System.out.println("2.switch 表达式 + 模式匹配:");
        Object obj = getObject();
        // 直接返回值，无需 break，箭头语法，自动类型转换
        switch (obj) {
            case Long i -> System.out.println("long");
            case Double i -> System.out.println("double");
            case Float i -> System.out.println("float");
            case Integer i -> System.out.println("int:" + i);
            case Byte i -> System.out.println("byte");
            case Short i -> System.out.println("short");
            case Character i -> System.out.println("char");
            case Boolean i -> System.out.println("boolean");
            case null -> System.out.println("null");
            default -> System.out.println("default value" + obj);
        }
        System.out.println("********************java 25********************");
    }

    static class FlexibleSupper extends Date {
        public FlexibleSupper(int value) {
            if (value < 0) throw new IllegalArgumentException("Must be positive");
            super();
        }
    }
    public static void supportExclusiveFeatures() {
        System.out.println("supportExclusiveFeatures:");
        System.out.println("""
1.终于允许在构造函数的 super() 调用之前执行语句
Example:
public FlexibleSupper(int value) {
    if (value < 0) throw new IllegalArgumentException("Must be positive");
    super();
}

""");
        new FlexibleSupper(1);

        System.out.println("""
2.引入 import module xxx; 可以一次性导入模块下的所有包，简化头部引用。
Example:
import module java.base;

3.无需 public class，无需 static，无需 String[] args：
void main() {
    println("Hello Java 25");
}""");
    }

    public static void supportConcurrency() throws InterruptedException {
        System.out.println("*****并发编程：虚拟线程（重点）");
        System.out.println("""
Java 8: 线程是昂贵的操作系统资源（OS Thread）。你需要小心翼翼地配置线程池，如果遇到阻塞 I/O（如数据库调用），线程会被挂起，浪费资源。
Java 25: 虚拟线程 (Virtual Threads) 是 JVM 管理的轻量级线程。你可以创建数百万个虚拟线程。当遇到 I/O 阻塞时，JVM 自动挂起该虚拟线程，底层 OS 线程去处理其他任务。

- 平台线程适合 CPU 密集型任务（如计算、排序）；
- 虚拟线程适合 IO 密集型任务（如接口调用、数据库查询、文件读写）—— 这类任务大部分时间在 “等待”（如等待网络响应），虚拟线程可将等待时间的资源释放给其他虚拟线程。

虚拟线程的关键 API:
1.Thread.ofVirtual():创建虚拟线程构建器
2.Thread.ofPlatform():创建平台线程构建器（对比用）
3.Executors.newVirtualThreadPerTaskExecutor():创建虚拟线程池（每个任务一个虚拟线程）
4.Thread.currentThread().isVirtual():判断当前线程是否为虚拟线程

虚拟现场陷阱：
陷阱 1：长时间占用 CPU（阻塞调度）：
虚拟线程是协作式调度，若虚拟线程长时间执行 CPU 密集型任务（无阻塞），会占用绑定的平台线程，导致其他虚拟线程无法调度。
✅ 解决：CPU 密集型任务仍用平台线程，仅 IO 密集型任务用虚拟线程。

陷阱 2：使用不兼容的阻塞操作
部分老旧库的阻塞操作（如 synchronized 同步块、FileInputStream 旧版阻塞）会导致虚拟线程 “固定” 在平台线程上（无法解绑），失去轻量优势。
✅ 解决：
- 优先使用 ReentrantLock 替代 synchronized（Java 21 已优化 synchronized，但仍建议）；
- 使用 NIO 替代旧版 IO（如 FileChannel 替代 FileInputStream）。

陷阱 3：无限制创建虚拟线程
虽然虚拟线程轻量，但创建百万级以上仍会占用内存（栈扩容、线程对象本身）。
✅ 解决：通过 ExecutorService 管理，避免手动创建大量虚拟线程。

3. 虚拟线程的监控
JDK 提供工具监控虚拟线程：
- jcmd：jcmd <PID> Thread.print 可打印所有虚拟线程；
- JConsole/JVisualVM：Java 21+ 已支持虚拟线程监控；
- ThreadMXBean：编程式获取虚拟线程信息：
~~~java
ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
// 获取所有线程 ID（包含虚拟线程）
long[] threadIds = threadMXBean.getAllThreadIds();
for (long tid : threadIds) {
    ThreadInfo info = threadMXBean.getThreadInfo(tid);
    if (info != null && Thread.currentThread().isVirtual()) {
        System.out.println("虚拟线程：" + info.getThreadName());
    }
}
~~~
                """);
        System.out.println("直接创建（Thread.ofVirtual ()）：");
        Thread virtualThread = Thread.ofVirtual()
                .name("my-virtual-thread")
                .start(() -> {
                    System.out.println("my-virtual-thread start:" + Thread.currentThread());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("my-virtual-thread end");
                });
        // 等待虚拟线程结束（与平台线程用法一致）
        virtualThread.join();

        System.out.println("通过 ExecutorService 创建（推荐）:");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 提交 100 万个虚拟线程任务（无压力）
            for (int i = 0; i < 1_000_000; i++) {
                int taskId = i;
                executor.submit(() -> {
                    System.out.println("taskId " + taskId + " running in: " + Thread.currentThread());
                    // 即使阻塞(Thread.sleep)，底层系统线程也会被释放去干别的事
                    // 极其廉价，像 Go 语言的协程
                    Thread.sleep(100);
                    return taskId;
                });
            }
        }

        System.out.println("构建未启动的虚拟线程:");
        // 构建虚拟线程（未启动）
        Thread unstartedVirtualThread = Thread.ofVirtual()
                .unstarted(() -> System.out.println("虚拟线程未启动，手动调用 start() 执行"));
        // 手动启动
        unstartedVirtualThread.start();

        System.out.println("结构化并发Structured Concurrency：");
//        try (var scope = new StructuredTaskScope.Shut)
//        StructuredTaskScope
    }

    public static void supportMoreYouDontSee () {
        System.out.println("""
还有一些不能直观看见的提升：
1.Compact Object Headers (JEP 519 - Java 25 正式特性):
- Java 8 对象头通常占用 96-128 bits。[6]
- Java 25 将其压缩至 64 bits。这直接减少了 10%-20% 的堆内存占用，大幅提升 CPU 缓存命中率。

2.垃圾回收 (GC):
- Java 8: 默认 ParallelGC，G1 尚不成熟。STW (Stop-The-World) 停顿经常达到数百毫秒甚至秒级。
- Java 25:
    - ZGC: 无论堆多大（TB级），停顿时间由 <1ms 保证。
    - Generational Shenandoah: 针对高吞吐低延迟场景的另一选择。

3.启动速度:
Java 25 引入了改进的 CDS (Class Data Sharing) 和 AOT Method Profiling，使得应用启动速度比 Java 8 快 30% 以上，特别适合微服务和 Serverless 场景。
                """);
    }

    public static void supportMoreBetterUtil () {
        System.out.println("""
1.HTTP 请求：
- java8：HttpURLConnection (难用，不支持 HTTP/2)
- java25：HttpClient (Java 11引入，支持异步、HTTP/2)

2.文本块：
- java8：拼接字符串 str = "{\\n" + " \\"id\\":1\\n" + "}"
- java25：多行字符串，原生 JSON/SQL 支持

3.集合创建：
- java8：
- java25：List.of(), Map.of(), Set.of() (真正的不可变)

4.Stream API：
- java8：collect(Collectors.toList())
- java25：toList() (Java 16), Gatherers (Java 24/25 提供更强流处理)

5.Null 安全：
- java8：Optional 刚引入，功能有限
- java25：Optional.ifPresentOrElse, Optional.stream
                """);
    }

    void main() {
        String str = """
一句话评价：Java 8 是经典的“蒸汽机”，稳定但笨重；Java 25 是现代化的“核聚变引擎”，轻量、高效且强大。

综合建议:
新项目：直接使用 Java 25。利用 record、虚拟线程和新 API，开发效率和运行效率双高。
老项目 (Java 8)：
如果项目处于维护模式，非必要不重构。
如果项目仍在活跃开发，建议跳过 11/17，直接升级到 21 或 25。Java 25 提供的迁移工具（如 jdeprscan）和兼容性模式已经非常成熟。
注意：从 Java 8 升级最大的痛点在于 模块化系统 (Java 9) 的引入和 sun.misc.Unsafe 的封装。许多老旧依赖库需要更新版本。
                """;
    }
}
