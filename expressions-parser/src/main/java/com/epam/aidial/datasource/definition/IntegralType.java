package com.epam.aidial.datasource.definition;

public enum IntegralType {
    INT8(1),
    UINT8(2),
    INT16(3),
    UINT16(4),
    INT32(5),
    UINT32(6),
    INT64(7),
    UINT64(8);

    private final int number;

    IntegralType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
