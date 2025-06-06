package com.epam.aidial.metric.service.influx;

import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.expressions.impl.NumberConstantImpl;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.impl.BinaryComparisonFilterImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.CONTAINS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.ENDS_WITH;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.GREATER;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.GREATER_OR_EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.LESS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.LESS_OR_EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.LIKE;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.NOT_CONTAINS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.NOT_EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.STARTS_WITH;
import static org.assertj.core.api.Assertions.assertThat;

class FluxConditionPartBuilderTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("createFilterPart_CreatesCorrectFilterQuery_Cases")
    void createFilterPart_CreatesCorrectFilterQuery(String caseName, Filter filter, String expected) {
        var actual = FluxConditionPartBuilder.createFilterPart(filter).getQuery();
        assertThat(actual).isEqualTo(expected);
    }


    public static Stream<Arguments> createFilterPart_CreatesCorrectFilterQuery_Cases() {
        var stringColumn = new ColumnImpl(Type.STRING, "x");
        var stringConst = new ConstantImpl(Type.STRING, "value");

        var numericColumn = new ColumnImpl(Type.INT_64, "x");
        var numericConst = NumberConstantImpl.valueOf(123L);

        return Stream.of(
                Arguments.of("equals",
                        BinaryComparisonFilterImpl.of(stringColumn, EQUALS, stringConst),
                        "|> filter(fn: (r) => r[\"x\"] == \"value\")"
                ), Arguments.of("not equals",
                        BinaryComparisonFilterImpl.of(stringColumn, NOT_EQUALS, stringConst),
                        "|> filter(fn: (r) => r[\"x\"] != \"value\")"
                ), Arguments.of("like => equals",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "value")),
                        "|> filter(fn: (r) => r[\"x\"] == \"value\")"
                ), Arguments.of("like => starts with",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "value%")),
                        "|> filter(fn: (r) => exists r[\"x\"] and strings.hasPrefix(v: r[\"x\"], prefix: \"value\"))"
                ), Arguments.of("like => ends with",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "%value")),
                        "|> filter(fn: (r) => exists r[\"x\"] and strings.hasSuffix(v: r[\"x\"], suffix: \"value\"))"
                ), Arguments.of("like => contains",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "%value%")),
                        "|> filter(fn: (r) => exists r[\"x\"] and strings.containsStr(v: r[\"x\"], substr: \"value\"))"
                ), Arguments.of("contains",
                        BinaryComparisonFilterImpl.of(stringColumn, CONTAINS, stringConst),
                        "|> filter(fn: (r) => exists r[\"x\"] and strings.containsStr(v: r[\"x\"], substr: \"value\"))"
                ), Arguments.of("not contains",
                        BinaryComparisonFilterImpl.of(stringColumn, NOT_CONTAINS, stringConst),
                        "|> filter(fn: (r) => exists r[\"x\"] and not strings.containsStr(v: r[\"x\"], substr: \"value\"))"
                ), Arguments.of("starts with",
                        BinaryComparisonFilterImpl.of(stringColumn, STARTS_WITH, stringConst),
                        "|> filter(fn: (r) => exists r[\"x\"] and strings.hasPrefix(v: r[\"x\"], prefix: \"value\"))"
                ), Arguments.of("ends with",
                        BinaryComparisonFilterImpl.of(stringColumn, ENDS_WITH, stringConst),
                        "|> filter(fn: (r) => exists r[\"x\"] and strings.hasSuffix(v: r[\"x\"], suffix: \"value\"))"
                ), Arguments.of("greater than",
                        BinaryComparisonFilterImpl.of(numericColumn, GREATER, numericConst),
                        "|> filter(fn: (r) => r[\"x\"] > 123.0)"
                ), Arguments.of("greater than or equals",
                        BinaryComparisonFilterImpl.of(numericColumn, GREATER_OR_EQUALS, numericConst),
                        "|> filter(fn: (r) => r[\"x\"] >= 123.0)"
                ), Arguments.of("less than",
                        BinaryComparisonFilterImpl.of(numericColumn, LESS, numericConst),
                        "|> filter(fn: (r) => r[\"x\"] < 123.0)"
                ), Arguments.of("less than or equals",
                        BinaryComparisonFilterImpl.of(numericColumn, LESS_OR_EQUALS, numericConst),
                        "|> filter(fn: (r) => r[\"x\"] <= 123.0)"
                )
        );
    }

}