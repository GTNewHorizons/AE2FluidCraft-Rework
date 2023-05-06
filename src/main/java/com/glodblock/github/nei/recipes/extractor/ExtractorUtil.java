package com.glodblock.github.nei.recipes.extractor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.glodblock.github.nei.object.OrderStack;

import codechicken.nei.PositionedStack;

public final class ExtractorUtil {

    public static List<OrderStack<?>> packItemStack(List<PositionedStack> items) {
        AtomicInteger index = new AtomicInteger(-1);
        return items.stream().map(item -> {
            index.getAndIncrement();
            return OrderStack.pack(item, index.get());
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<OrderStack<?>> packItemStack(List<PositionedStack> items,
            Function<PositionedStack, Object> transformer) {
        AtomicInteger index = new AtomicInteger(-1);
        return items.stream().map(item -> {
            index.getAndIncrement();
            Object o = transformer.apply(item);
            return o != null ? new OrderStack<>(o, index.get(), item.items) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
