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

import java.util.List;
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
    void createFilterPart_CreatesCorrectFilterQuery(String caseName, Filter filter,
                                                     List<String> expectedPreamble, String expectedQuery) {
        var actual = FluxConditionPartBuilder.createFilterPart(filter);
        assertThat(actual.getPreamble()).isEqualTo(expectedPreamble);
        assertThat(actual.getQuery()).isEqualTo(expectedQuery);
    }


    public static Stream<Arguments> createFilterPart_CreatesCorrectFilterQuery_Cases() {
        var stringColumn = new ColumnImpl(Type.STRING, "x");
        var stringConst = new ConstantImpl(Type.STRING, "value");

        var numericColumn = new ColumnImpl(Type.INT_64, "x");
        var numericConst = NumberConstantImpl.valueOf(123L);

        return Stream.of(
                Arguments.of("equals",
                        BinaryComparisonFilterImpl.of(stringColumn, EQUALS, stringConst),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] == \"value\")"
                ), Arguments.of("not equals",
                        BinaryComparisonFilterImpl.of(stringColumn, NOT_EQUALS, stringConst),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] != \"value\")"
                ), Arguments.of("like => equals",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "value")),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] == \"value\")"
                ), Arguments.of("like => starts with",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "value%")),
                        List.of("_re0 = regexp.compile(v: \"(?i)^value\")"),
                        "|> filter(fn: (r) => r[\"x\"] =~ _re0)"
                ), Arguments.of("like => ends with",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "%value")),
                        List.of("_re0 = regexp.compile(v: \"(?i)value$\")"),
                        "|> filter(fn: (r) => r[\"x\"] =~ _re0)"
                ), Arguments.of("like => contains",
                        BinaryComparisonFilterImpl.of(stringColumn, LIKE, new ConstantImpl(Type.STRING, "%value%")),
                        List.of("_re0 = regexp.compile(v: \"(?i)value\")"),
                        "|> filter(fn: (r) => r[\"x\"] =~ _re0)"
                ), Arguments.of("contains",
                        BinaryComparisonFilterImpl.of(stringColumn, CONTAINS, stringConst),
                        List.of("_re0 = regexp.compile(v: \"(?i)value\")"),
                        "|> filter(fn: (r) => r[\"x\"] =~ _re0)"
                ), Arguments.of("not contains",
                        BinaryComparisonFilterImpl.of(stringColumn, NOT_CONTAINS, stringConst),
                        List.of("_re0 = regexp.compile(v: \"(?i)value\")"),
                        "|> filter(fn: (r) => r[\"x\"] !~ _re0)"
                ), Arguments.of("starts with",
                        BinaryComparisonFilterImpl.of(stringColumn, STARTS_WITH, stringConst),
                        List.of("_re0 = regexp.compile(v: \"(?i)^value\")"),
                        "|> filter(fn: (r) => r[\"x\"] =~ _re0)"
                ), Arguments.of("ends with",
                        BinaryComparisonFilterImpl.of(stringColumn, ENDS_WITH, stringConst),
                        List.of("_re0 = regexp.compile(v: \"(?i)value$\")"),
                        "|> filter(fn: (r) => r[\"x\"] =~ _re0)"
                ), Arguments.of("greater than",
                        BinaryComparisonFilterImpl.of(numericColumn, GREATER, numericConst),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] > 123.0)"
                ), Arguments.of("greater than or equals",
                        BinaryComparisonFilterImpl.of(numericColumn, GREATER_OR_EQUALS, numericConst),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] >= 123.0)"
                ), Arguments.of("less than",
                        BinaryComparisonFilterImpl.of(numericColumn, LESS, numericConst),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] < 123.0)"
                ), Arguments.of("less than or equals",
                        BinaryComparisonFilterImpl.of(numericColumn, LESS_OR_EQUALS, numericConst),
                        List.of(),
                        "|> filter(fn: (r) => r[\"x\"] <= 123.0)"
                )
        );
    }

}
