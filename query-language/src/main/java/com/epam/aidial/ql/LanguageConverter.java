package com.epam.aidial.ql;

import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.ParseUtils;
import com.epam.aidial.expressions.exceptions.ParseException;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.FromDto;
import com.epam.aidial.ql.dto.JoinDto;
import com.epam.aidial.ql.dto.LimitByDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.SortDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TableDto;
import com.epam.aidial.ql.dto.TupleDto;
import com.epam.aidial.ql.dto.UnionAllDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import com.epam.aidial.ql.dto.filters.OrDto;
import com.epam.aidial.ql.dto.filters.UnaryComparisonFilterDto;
import com.epam.aidial.ql.helpers.DtoHelper;
import com.epam.aidial.ql.helpers.ExpressionUtils;
import com.epam.aidial.ql.helpers.ValidationUtils;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.From;
import com.epam.aidial.ql.model.Join;
import com.epam.aidial.ql.model.LimitBy;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Sort;
import com.epam.aidial.ql.model.Table;
import com.epam.aidial.ql.model.Tuple;
import com.epam.aidial.ql.model.UnionAll;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.BinaryComparisonFilterImpl;
import com.epam.aidial.ql.model.filters.impl.NotImpl;
import com.epam.aidial.ql.model.filters.impl.OrImpl;
import com.epam.aidial.ql.model.filters.impl.UnaryComparisonFilterImpl;
import com.epam.aidial.ql.model.impl.JoinImpl;
import com.epam.aidial.ql.model.impl.LimitByImpl;
import com.epam.aidial.ql.model.impl.QueryImpl;
import com.epam.aidial.ql.model.impl.SortImpl;
import com.epam.aidial.ql.model.impl.TupleImpl;
import com.epam.aidial.ql.model.impl.UnionAllImpl;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

// TODO: possible split validation and language conversion logic
public class LanguageConverter {
    private final Engine engine;

    public LanguageConverter(final Engine engine) {
        this.engine = engine;
    }

    public Completable convert(final CompletableDto completable, final Map<String, Table> tables) throws ParseException {
        if (completable instanceof QueryDto) {
            return convert((QueryDto) completable, tables);
        } else if (completable instanceof UnionAllDto) {
            return convert((UnionAllDto) completable, tables);
        }
        throw new NotImplementedException(completable.getClass().getName());
    }

    private From convert(final FromDto from, final Map<String, Table> tables) throws ParseException {
        if (from instanceof CompletableDto) {
            return convert((CompletableDto) from, tables);
        } else if (from instanceof TableDto) {
            return convert((TableDto) from, tables);
        }
        throw new NotImplementedException(from.getClass().getName());
    }

    public boolean validateExpressions(final List<? extends Expression> expressions,
                                       final List<? extends ExpressionDto> requested,
                                       final List<? extends Expression> groupBy) {
        boolean shouldGroup = false;
        ExpressionDto firstUnderAggregationFunction = null;
        final Set<Expression> groupExpressions;
        if (groupBy.isEmpty()) {
            groupExpressions = Collections.emptySet();
        } else {
            groupExpressions = new HashSet<>(groupBy);
            shouldGroup = true;
        }

        for (int i = 0; i < expressions.size(); i++) {
            final Expression expression = expressions.get(i);
            final boolean aggregation = expression.isAggregation() || isGroupFunction(expression);
            if (!aggregation && (groupExpressions.contains(expression)
                    || groupExpressions.containsAll(expression.getDependentColumns())
                    || isAliasCoveredByGroupBy(expression, groupExpressions))) {
                continue;
            } else if (!aggregation) {
                if (shouldGroup) {
                    throw new ParseException(String.format("Column '%s' is not under aggregate function and not in grouping cause",
                            ((StringExpressionDto) requested.get(i)).getExpression()));
                }
                if (firstUnderAggregationFunction == null) {
                    firstUnderAggregationFunction = requested.get(i);
                }
            } else {
                if (firstUnderAggregationFunction != null) {
                    throw new ParseException(String.format("Column '%s' is not under aggregate function and not in grouping cause",
                            ((StringExpressionDto) firstUnderAggregationFunction).getExpression()));
                }
                shouldGroup = true;
            }
        }
        return shouldGroup;
    }

    private boolean isGroupFunction(Expression expression) {
        return expression instanceof Alias alias && alias.getExpression() instanceof GroupFunctionCall
               || expression instanceof GroupFunctionCall;
    }

    // The user may write `groupBy: ["proj"]` referencing the alias name from
    // `expressions: ["project_id as proj"]`. The groupBy parser resolves "proj"
    // to a fresh Column("proj") rather than the original Alias object, so neither
    // direct membership nor dependent-column coverage matches. Treat the alias as
    // covered when groupBy contains a Column whose name equals the alias name.
    private boolean isAliasCoveredByGroupBy(Expression expression, Set<Expression> groupExpressions) {
        if (!(expression instanceof Alias alias)) {
            return false;
        }
        return groupExpressions.stream()
                .anyMatch(g -> g instanceof Column column && alias.getName().equals(column.getName()));
    }

    private Query convert(final QueryDto query, final Map<String, Table> tables) throws ParseException {
        final From from = convert(query.getFrom(), tables);
        final Join join;
        final Map<String, Column> availableColumns = new HashMap<>(from.getColumns());
        if (query.getJoin() != null) {
            join = convert(query.getJoin(), tables, availableColumns);
            final Map<String, ? extends Column> right = join.getFrom().getColumns();
            //TODO check that all columnNames are different.
            availableColumns.putAll(right);
        } else {
            join = null;
        }
        final List<Expression> expressions = parseExpressions(query.getExpressions(), tables, availableColumns, true);
        availableColumns.putAll(ExpressionUtils.getColumns(expressions));

        final List<Expression> groupBy = parseExpressions(query.getGroupBy(), tables, availableColumns, false);
        for (int i = 0; i < groupBy.size(); i++) {
            ValidationUtils.verifyAllowGrouping(groupBy.get(i), DtoHelper.expressionToName(query.getGroupBy().get(i)));
        }
        validateExpressions(expressions, query.getExpressions(), groupBy);

        if (query.isWithTotals() && groupBy.isEmpty()) {
            throw new ParseException("WITH TOTALS can be used with GROUP BY only.");
        }

        final Filter preScale = convertPreScale(query.getPreScale(), tables, availableColumns);
        final Filter where = convertWhere(query.getWhere(), tables, availableColumns);
        final Filter having = convertHaving(query.getHaving(), tables, availableColumns);
        final List<Sort> orderBy = convert(query.getOrderBy(), tables, availableColumns);
        final LimitBy limitBy = query.getLimitBy() != null ? convert(query.getLimitBy(), tables, availableColumns) : null;

        return QueryImpl.builder()
                .distinct(query.isDistinct())
                .expressions(expressions)
                .from(from)
                .join(join)
                .preScale(preScale)
                .where(where)
                .groupBy(groupBy)
                .withTotals(query.isWithTotals())
                .having(having)
                .orderBy(orderBy)
                .limitBy(limitBy)
                .offset(query.getOffset())
                .limit(query.getLimit())
                .build();
    }

    private UnionAll convert(final UnionAllDto unionAll, final Map<String, Table> tables) throws ParseException {
        final List<Completable> completables = unionAll.getQueries().stream()
                .map(query -> convert(query, tables))
                .collect(Collectors.toList());
        return UnionAllImpl.builder().queries(completables).build();
    }

    private Table convert(final TableDto table, final Map<String, Table> tables) throws ParseException {
        final Table result = tables.get(table.getName());
        if (result == null) {
            throw new ParseException(String.format("Table `%s` is not found", table.getName()));
        }
        return result;
    }

    private Join convert(final JoinDto join, final Map<String, Table> tables, final Map<String, ? extends Column> columns) {
        final From rightFrom = convert(join.getFrom(), tables);
        return JoinImpl.builder()
                .strictness(join.getStrictness())
                .type(join.getType())
                .from(rightFrom)
                .leftExpressions(parseExpressions(join.getLeft(), tables, columns, false))
                .rightExpressions(parseExpressions(join.getRight(), tables, rightFrom.getColumns(), false))
                .build();
    }

    private List<Expression> parseExpressions(final List<ExpressionDto> expressions,
                                              final Map<String, Table> tables,
                                              final Map<String, ? extends Column> columns,
                                              final boolean allowAlias) {
        return expressions.stream().map(x -> convert(x, tables, columns, allowAlias)).collect(Collectors.toList());
    }

    private Expression convert(final ExpressionDto expression,
                               final Map<String, Table> tables,
                               final Map<String, ? extends Column> columns,
                               final boolean allowAlias) {
        if (expression instanceof StringExpressionDto) {
            return ParseUtils.parseExpression(((StringExpressionDto) expression).getExpression(), engine.getFunctions(), columns, allowAlias).getExpression();
        } else if (expression instanceof CompletableDto) {
            return convert((CompletableDto) expression, tables);
        } else if (expression instanceof TupleDto) {
            return convert((TupleDto) expression, tables, columns);
        }
        throw new NotImplementedException(expression.getClass().getName());
    }

    private Tuple convert(final TupleDto tuple, final Map<String, Table> tables, final Map<String, ? extends Column> columns) {
        return TupleImpl.builder().expressions(parseExpressions(tuple, tables, columns, false)).build();
    }

    public Filter convertPreScale(final @Nullable FilterDto filter,
                                  final Map<String, Table> tables,
                                  final Map<String, ? extends Column> columns) {
        if (filter == null) {
            return null;
        }
        return convert(filter, tables, columns, (expression, dto) ->
                ValidationUtils.verifyAllowPrescale(expression, DtoHelper.expressionToName(dto))
        );
    }

    public Filter convertWhere(final @Nullable FilterDto filter,
                               final Map<String, Table> tables,
                               final Map<String, ? extends Column> columns) {
        if (filter == null) {
            return null;
        }
        return convert(filter, tables, columns, (expression, dto) ->
                ValidationUtils.verifyAllowWhere(expression, DtoHelper.expressionToName(dto))
        );
    }

    public Filter convertHaving(final @Nullable FilterDto filter,
                                final Map<String, Table> tables,
                                final Map<String, ? extends Column> columns) {
        if (filter == null) {
            return null;
        }
        return convert(filter, tables, columns, (expression, dto) ->
                ValidationUtils.verifyAllowHaving(expression, DtoHelper.expressionToName(dto))
        );
    }

    private Filter convert(final FilterDto filter,
                           final Map<String, Table> tables,
                           final Map<String, ? extends Column> columns,
                           final BiConsumer<Expression, ExpressionDto> doWithExpression) {
        if (filter instanceof AndDto) {
            return AndImpl.of(
                    ((AndDto) filter).stream().map(x -> convert(x, tables, columns, doWithExpression)).collect(Collectors.toList())
            );
        } else if (filter instanceof OrDto) {
            return OrImpl.of(
                    ((OrDto) filter).stream().map(x -> convert(x, tables, columns, doWithExpression)).collect(Collectors.toList())
            );
        } else if (filter instanceof NotDto) {
            return NotImpl.of(
                    convert(((NotDto) filter).getFilter(), tables, columns, doWithExpression)
            );
        } else if (filter instanceof BinaryComparisonFilterDto comparisonFilter) {
            final Expression left = convert(comparisonFilter.getLeft(), tables, columns, false);
            doWithExpression.accept(left, comparisonFilter.getLeft());
            final Expression right = convert(comparisonFilter.getRight(), tables, columns, false);
            doWithExpression.accept(right, comparisonFilter.getRight());
            ValidationUtils.verifySupportOperator(
                    comparisonFilter.getOperator(), left, right,
                    DtoHelper.expressionToName(comparisonFilter.getLeft()),
                    DtoHelper.expressionToName(comparisonFilter.getRight())
            );
            return BinaryComparisonFilterImpl.builder()
                    .leftExpression(left)
                    .operator(comparisonFilter.getOperator())
                    .rightExpression(right)
                    .build();
        } else if (filter instanceof UnaryComparisonFilterDto comparisonFilter) {
            final Expression expression = convert(comparisonFilter.getExpression(), tables, columns, false);
            if (doWithExpression != null) {
                doWithExpression.accept(expression, comparisonFilter.getExpression());
            }
            return UnaryComparisonFilterImpl.builder()
                    .expression(expression)
                    .operator(comparisonFilter.getOperator())
                    .build();
        }
        throw new NotImplementedException(filter.getClass().getName());
    }

    public List<Sort> convert(final List<SortDto> sorts,
                              final Map<String, Table> tables,
                              final Map<String, ? extends Column> columns) {
        return sorts.stream().map(sort -> {
            final Expression expression = convert(sort.getExpression(), tables, columns, false);
            ValidationUtils.verifyAllowOrderBy(expression, DtoHelper.expressionToName(sort.getExpression()));
            return SortImpl.builder()
                    .direction(sort.getDirection())
                    .expression(expression)
                    .build();
        }).collect(Collectors.toList());
    }

    private LimitBy convert(final LimitByDto limitBy, final Map<String, Table> tables, final Map<String, ? extends Column> columns) {
        return LimitByImpl.of(parseExpressions(limitBy.getExpressions(), tables, columns, false), limitBy.getLimit());
    }
}
