package com.epam.aidial.ql.helpers;

import com.epam.aidial.ql.dto.ColumnMetaDto;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.DataDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.FromDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TableDto;
import com.epam.aidial.ql.dto.TupleDto;
import com.epam.aidial.ql.dto.UnionAllDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import com.epam.aidial.ql.dto.filters.OrDto;
import com.epam.aidial.ql.dto.filters.UnaryComparisonFilterDto;
import com.epam.aidial.ql.model.Data;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DtoHelper {
    public static Set<String> getReportNames(final CompletableDto from) {
        final Set<String> reportNames = new HashSet<>();
        getReportNames((FromDto) from, reportNames);
        return reportNames;
    }

    public static Set<String> getReportNames(final FilterDto filter) {
        final Set<String> reportNames = new HashSet<>();
        getReportNames(filter, reportNames);
        return reportNames;
    }

    private static void getReportNames(final FromDto from, final Set<String> reportNames) {
        if (from instanceof TableDto) {
            reportNames.add(((TableDto) from).getName());
        } else if (from instanceof UnionAllDto) {
            ((UnionAllDto) from).getQueries().forEach(x -> getReportNames((FromDto) x, reportNames));
        } else if (from instanceof QueryDto) {
            final QueryDto query = (QueryDto) from;
            query.getExpressions().forEach(x -> getReportNames(x, reportNames));
            getReportNames(query.getFrom(), reportNames);
            if (query.getJoin() != null) {
                getReportNames(query.getJoin().getFrom(), reportNames);
            }
            if (query.getWhere() != null) {
                getReportNames(query.getWhere(), reportNames);
            }
            query.getGroupBy().forEach(x -> getReportNames(x, reportNames));
            if (query.getHaving() != null) {
                getReportNames(query.getHaving(), reportNames);
            }
            query.getOrderBy().forEach(x -> getReportNames(x.getExpression(), reportNames));
            if (query.getLimitBy() != null) {
                query.getLimitBy().getExpressions().forEach(x -> getReportNames(x, reportNames));
            }
        }
    }

    private static void getReportNames(final ExpressionDto expression, final Set<String> reportNames) {
        if (expression instanceof CompletableDto) {
            getReportNames((FromDto) expression, reportNames);
        } else if (expression instanceof TupleDto) {
            ((TupleDto) expression).forEach(x -> getReportNames(x, reportNames));
        }
    }

    private static void getReportNames(final FilterDto filter, final Set<String> reportNames) {
        if (filter instanceof AndDto) {
            ((AndDto) filter).forEach(x -> getReportNames(x, reportNames));
        } else if (filter instanceof OrDto) {
            ((OrDto) filter).forEach(x -> getReportNames(x, reportNames));
        } else if (filter instanceof NotDto) {
            getReportNames(((NotDto) filter).getFilter(), reportNames);
        } else if (filter instanceof BinaryComparisonFilterDto) {
            final BinaryComparisonFilterDto comparisonFilter = (BinaryComparisonFilterDto) filter;
            getReportNames(comparisonFilter.getLeft(), reportNames);
            getReportNames(comparisonFilter.getRight(), reportNames);
        } else if (filter instanceof UnaryComparisonFilterDto) {
            getReportNames(((UnaryComparisonFilterDto) filter).getExpression(), reportNames);
        }
    }

    public static DataDto convertToDto(final Data data, final CompletableDto completable) {
        final List<ExpressionDto> expressions;
        if (completable instanceof QueryDto) {
            expressions = ((QueryDto) completable).getExpressions();
            final List<ColumnMetaDto> columnMetaDtos = new ArrayList<>();
            for (int i = 0; i < expressions.size(); i++) {
                final ExpressionDto expression = expressions.get(i);
                columnMetaDtos.add(new ColumnMetaDto(expressionToName(expression), data.getExpressions().get(i).getType()));
            }
            return new DataDto(columnMetaDtos, data.getData());
        } else if (completable instanceof UnionAllDto) {
            return convertToDto(data, ((UnionAllDto) completable).getQueries().get(0));
        }
        throw new NotImplementedException(completable.toString());
    }

    public static String expressionToName(final ExpressionDto expression) {
        return expression instanceof StringExpressionDto ? ((StringExpressionDto) expression).getExpression() : "Subquery";
    }
}
