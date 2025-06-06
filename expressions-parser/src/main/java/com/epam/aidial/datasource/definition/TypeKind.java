package com.epam.aidial.datasource.definition;

public enum TypeKind {
    TYPE(0),
    BOOLEAN(1),
    INT8(2),
    UINT8(3),
    INT16(4),
    UINT16(5),
    INT32(6),
    UINT32(7),
    INT64(8),
    UINT64(9),
    FLOAT32(10),
    FLOAT64(11),
    DECIMAL(12),
    TEXT(13),
    DATA(14),
    TIMESTAMP(15),
    DATE(16),
    TIME(17),
    DURATION(18),
    UUID(19),

    NULLABLE(100),
    LIST(101),
    ACTION(102),
    FUNCTION(103),
    MAP(104),
    SET(105),

    ENUMERATION(1000),
    INTERFACE(1001),
    CLASS(1002),
    DECORATOR(1003);

    private final int number;

    TypeKind(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public IntegralType toIntegralType() {
        switch (this) {
            case INT8:
                return IntegralType.INT8;

            case UINT8:
                return IntegralType.UINT8;

            case INT16:
                return IntegralType.INT16;

            case UINT16:
                return IntegralType.UINT16;

            case INT32:
                return IntegralType.INT32;

            case UINT32:
                return IntegralType.UINT32;

            case INT64:
                return IntegralType.INT64;

            case UINT64:
                return IntegralType.UINT64;

            default:
                throw new IllegalStateException("Type '" + this + "' is not integral.");
        }
    }
}
