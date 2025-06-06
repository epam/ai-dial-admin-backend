package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.enums.Type;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ColumnImpl implements Column {
    private final Type type;
    private final String name;
}
