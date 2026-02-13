package org.example.azheng.anticheat.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class EvictingList<T> extends LinkedList<T> {
    private final int maxSize;

    // Create an empty EvictingList
    public EvictingList(int maxSize) {
        this.maxSize = maxSize;
    }

    //Create an EvictingList with elements in the collection c
    public EvictingList(Collection<? extends T> c, int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean add(T t) {
        if (size() >= maxSize) {
            removeFirst();
        }
        return super.add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return c.stream().anyMatch(this::add);
    }

    @Override
    public @NotNull Stream<T> stream() {
        return new CopyOnWriteArrayList<>(this).stream();
    }
}
