package com.epam.aidial.datasource.definition;

public enum LiteralKind {
    TYPE(1),
    BOOLEAN(2),
    TEXT(3),
    INT8(4),
    UINT8(5),
    INT16(6),
    UINT16(7),
    INT32(8),
    UINT32(9),
    INT64(10),
    UINT64(11),
    FLOAT32(12),
    FLOAT64(13),
    DECIMAL(14),
    TIMESTAMP(15),
    DATE(16),
    TIME(17),
    DURATION(18),
    UUID(19),
    NULL(20),
    ENUMERATION_VALUE(21),
    CONSTANT(22),
    LIST(23);

    private final int number;

    LiteralKind(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
