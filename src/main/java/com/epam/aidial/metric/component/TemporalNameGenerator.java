package com.epam.aidial.metric.component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class TemporalNameGenerator {

    private final Map<String, AtomicLong> nameCounter = new HashMap<>();

    public String generateNewName(String prefix) {
        nameCounter.putIfAbsent(prefix, new AtomicLong());
        var index = nameCounter.get(prefix).getAndIncrement();
        return prefix + index;
    }

}