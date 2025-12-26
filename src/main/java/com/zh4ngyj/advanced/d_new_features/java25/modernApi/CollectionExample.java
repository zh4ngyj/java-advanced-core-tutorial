package com.zh4ngyj.advanced.d_new_features.java25.modernApi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

public class CollectionExample {

    public static void collectionJava8() {
        List<String> list = Arrays.asList("a", "b", "c");
        // 注意：Arrays.asList 返回的列表不可新增/删除，但可以修改元素
        list.set(0, "new_a"); // 允许
        // list.add("d"); // 抛出 UnsupportedOperationException
    }

    public static void collectionJava25() {
        // 不可变 List
        List<String> list = List.of("a", "b", "c");
        // list.set(0, "new_a"); // 抛出 UnsupportedOperationException

        // 不可变 Map
        Map<String, Integer> map = Map.of("key1", 1, "key2", 2);
        System.out.println(map);
    }

    public static void streamJava8 () {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4);
        // 收集为 List
        List<Integer> evenNums = nums.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());
        System.out.println(evenNums);
    }

    public static void streamJava25() {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6);

        // 简化的 toList()（Java 16+）
        List<Integer> evenNums = nums.stream()
                .filter(n -> n % 2 == 0)
                .toList(); // 直接 toList()，返回不可变 List

        // Gatherers 分组（Java 25+）
        List<List<Integer>> grouped = nums.stream()
                .gather(Gatherers.windowFixed(2)) // 每 2 个元素分组
                .toList();
        System.out.println("分组结果：" + grouped); // [[1,2], [3,4], [5,6]]
    }

    public static void nullSafeJava8() {
        Optional<String> name = Optional.ofNullable(getName8());
        // 仅支持 ifPresent / orElse
        name.ifPresent(n -> System.out.println("名称：" + n));
        String defaultName = name.orElse("默认名称");
    }

    private static String getName8() {
        return null; // 模拟可能返回 null 的情况
    }

    private static String getName25() {
        return "Java 25";
    }

    public static void nullSafeJava25() {
        Optional<String> name = Optional.ofNullable(getName25());

        // ifPresentOrElse：存在时执行，不存在时执行另一个逻辑
        name.ifPresentOrElse(
                n -> System.out.println("名称：" + n),
                () -> System.out.println("名称为空")
        );

        // Optional.stream()：转为 Stream 处理
        String joined = name.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining());
        System.out.println("处理后：" + joined);
    }
}
