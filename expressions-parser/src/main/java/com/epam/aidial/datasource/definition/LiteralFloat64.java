package com.epam.aidial.datasource.definition;

public final class LiteralFloat64 extends Literal {
    private final double value;

    public LiteralFloat64(double value) {
        super(LiteralKind.FLOAT64);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (Double.isNaN(value))
            return "Float64.NaN";
        if (Double.isInfinite(value))
            return value > 0 ? "Float64.POSITIVE_INFINITY" : "Float64.NEGATIVE_INFINITY";
        return Double.toString(value) + "f64";
    }
}
