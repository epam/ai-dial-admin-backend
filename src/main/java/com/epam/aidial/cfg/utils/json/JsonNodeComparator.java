package com.epam.aidial.cfg.utils.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonNodeComparator {

    private final Set<List<String>> ignoreCollectionOrderPatternsPathSegments;

    public JsonNodeComparator(Set<String> ignoreCollectionOrderPatterns) {
        this.ignoreCollectionOrderPatternsPathSegments = ignoreCollectionOrderPatterns.stream()
                .map(this::parse)
                .collect(Collectors.toSet());
    }

    public boolean equals(JsonNode left, JsonNode right) {
        return equals(left, right, new ArrayList<>());
    }

    private boolean equals(JsonNode left, JsonNode right, List<String> pathSegments) {
        if (left == null || right == null) {
            return left == right;
        }

        if (left.getNodeType() != right.getNodeType()) {
            return false;
        }

        if (left.isObject()) {
            return objectsEqual(left, right, pathSegments);
        }

        if (left.isArray()) {
            return arraysEqual(left, right, pathSegments);
        }

        return left.equals(right);
    }

    private boolean objectsEqual(JsonNode left, JsonNode right, List<String> pathSegments) {
        if (left.size() != right.size()) {
            return false;
        }

        Iterator<String> fields = left.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            if (!right.has(field)) {
                return false;
            }

            pathSegments.add(field);
            boolean equals = equals(left.get(field), right.get(field), pathSegments);
            pathSegments.remove(pathSegments.size() - 1);

            if (!equals) {
                return false;
            }
        }
        return true;
    }

    private boolean arraysEqual(JsonNode left, JsonNode right, List<String> pathSegments) {
        if (left.size() != right.size()) {
            return false;
        }

        boolean ignoreCollectionOrder = ignoreCollectionOrderPatternsPathSegments.stream()
                .anyMatch(p -> matches(pathSegments, p));

        return ignoreCollectionOrder
                ? arraysEqualIgnoreOrder(left, right, pathSegments)
                : arraysEqualConsiderOrder(left, right, pathSegments);
    }

    private boolean arraysEqualConsiderOrder(JsonNode left, JsonNode right, List<String> pathSegments) {
        for (int i = 0; i < left.size(); i++) {
            if (!equals(left.get(i), right.get(i), pathSegments)) {
                return false;
            }
        }
        return true;
    }

    private boolean arraysEqualIgnoreOrder(JsonNode left, JsonNode right, List<String> pathSegments) {
        List<JsonNode> remaining = new ArrayList<>();
        right.forEach(remaining::add);

        for (JsonNode leftElement : left) {
            boolean matched = false;

            Iterator<JsonNode> it = remaining.iterator();
            while (it.hasNext()) {
                JsonNode rightElement = it.next();

                if (equals(leftElement, rightElement, pathSegments)) {
                    it.remove();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    private boolean matches(List<String> actualPathSegments, List<String> patternPathSegments) {
        if (actualPathSegments.size() != patternPathSegments.size()) {
            return false;
        }

        for (int i = 0; i < actualPathSegments.size(); i++) {
            String patternPathSegment = patternPathSegments.get(i);
            String actualPathSegment = actualPathSegments.get(i);

            if (!patternPathSegment.equals("*") && !patternPathSegment.equals(actualPathSegment)) {
                return false;
            }
        }
        return true;
    }

    private List<String> parse(String path) {
        return Arrays.asList(path.split("\\."));
    }
}