package com.epam.aidial.expressions.impl;

import org.jetbrains.annotations.NotNull;

public class ComparableByteArray implements Comparable<ComparableByteArray> {
        private final byte[] value;

        public ComparableByteArray(byte[] value) {
            this.value = value;
        }

        public byte[] getValue() {
            return value;
        }

        @Override
        public int compareTo(@NotNull ComparableByteArray that) {
            int len1 = value.length;
            int len2 = that.value.length;
            int lim = Math.min(len1, len2);

            for (int i = 0; i < lim; i++) {
                int t = Byte.compare(value[i], that.value[i]);
                if (t != 0) {
                    return t;
                }
            }
            return len1 - len2;
        }
    }
