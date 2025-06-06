package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnumerationMemberDef extends ItemDef {
    private Literal rawValue;
    private LiteralInteger value;

    public EnumerationMemberDef(@NotNull String name, @NotNull LiteralInteger value, @Nullable List<String> comments) {
        super(name, name, comments);
        this.value = value;
    }

    @NotNull
    Literal getRawValue() {
        return rawValue;
    }

    void setRawValue(Literal rawValue) {
        this.rawValue = rawValue;
    }

    @NotNull
    public LiteralInteger getValue() {
        return value;
    }

    void setValue(LiteralInteger value) {
        this.value = value;
    }
}
