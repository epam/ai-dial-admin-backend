package com.epam.aidial.ql.deserializers.sql;

import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.deserializers.sql.gen.QueryLanguageParser;
import com.epam.aidial.ql.deserializers.sql.gen.QueryLanguageParserBaseListener;
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
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class QueryLangageListenerImpl extends QueryLanguageParserBaseListener {
    private final Stack<Object> stack = new Stack<>();

    private int numberOfErrors = 0;
    private CompletableDto completable;

    public CompletableDto getCompletable() {
        return completable;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        ++numberOfErrors;
    }

    @Override
    public void exitRoot(QueryLanguageParser.RootContext ctx) {
        completable = (CompletableDto) stack.pop();
    }

    @Override
    public void exitUnion(QueryLanguageParser.UnionContext ctx) {
        final int cnt = (ctx.getChildCount() + 1) / 2;
        if (cnt > 1) {
            stack.add(new UnionAllDto(getFromStack(CompletableDto.class, cnt)));
        }
    }

    @Override
    public void enterTable(QueryLanguageParser.TableContext ctx) {
        stack.add(new TableDto(ctx.getText()));
    }

    @Override
    public void exitJoin(QueryLanguageParser.JoinContext ctx) {
        final JoinDto join = new JoinDto();
        join.setStrictness(JoinStrictness.valueOf(ctx.getChild(0).getText().toUpperCase()));
        join.setType(JoinType.valueOf(ctx.getChild(1).getText().toUpperCase()));
        join.setRight(extractExpressionsForJoin());
        join.setLeft(extractExpressionsForJoin());
        join.setFrom((FromDto) stack.pop());
        stack.add(join);
    }

    private List<ExpressionDto> extractExpressionsForJoin() {
        final Object object = stack.pop();
        if (object instanceof ExpressionDto) {
            return Collections.singletonList((ExpressionDto) object);
        } else {
            return (List<ExpressionDto>) object;
        }
    }

    @Override
    public void exitQuery(QueryLanguageParser.QueryContext ctx) {
        final QueryDto query = new QueryDto();
        boolean containHaving = false;
        boolean containOrderBy = false;
        boolean containGroupBy = false;
        boolean containWhere = false;
        for (final ParseTree tree : ctx.children) {
            if (tree instanceof TerminalNode) {
                switch (replaceSpaces(tree.getText().toUpperCase())) {
                    case "DISTINCT":
                        query.setDistinct(true);
                        break;
                    case "WITH_TOTALS":
                        query.setWithTotals(true);
                        break;
                    case "HAVING":
                        containHaving = true;
                        break;
                    case "ORDER_BY":
                        containOrderBy = true;
                        break;
                    case "GROUP_BY":
                        containGroupBy = true;
                        break;
                    case "WHERE":
                        containWhere = true;
                }
            }
        }

        if (ctx.getChild(ctx.getChildCount() - 1) instanceof QueryLanguageParser.Decimal_literalContext) {
            query.setLimit(Long.parseLong(ctx.getChild(ctx.getChildCount() - 1).getText()));
            if (ctx.getChild(ctx.getChildCount() - 3) instanceof QueryLanguageParser.Decimal_literalContext) {
                query.setOffset(Long.parseLong(ctx.getChild(ctx.getChildCount() - 3).getText()));
            }
        }

        Object object = stack.peek();
        if (object instanceof LimitByDto) {
            query.setLimitBy((LimitByDto) stack.pop());
            object = stack.peek();
        }
        if (containOrderBy) {
            query.setOrderBy((List<SortDto>) stack.pop());
            object = stack.peek();
        }
        if (containHaving) {
            query.setHaving((FilterDto) stack.pop());
            object = stack.peek();
        }
        if (containGroupBy) {
            query.setGroupBy((List<ExpressionDto>) stack.pop());
            object = stack.peek();
        }
        if (containWhere) {
            query.setWhere((FilterDto) stack.pop());
            object = stack.peek();
        }
        if (object instanceof FilterDto) {
            query.setPreScale((FilterDto) stack.pop());
            object = stack.peek();
        }
        if (object instanceof JoinDto) {
            query.setJoin((JoinDto) stack.pop());
        }
        query.setFrom((FromDto) stack.pop());
        query.setExpressions((List<ExpressionDto>) stack.pop());

        stack.add(query);
    }

    @Override
    public void exitExpressions(QueryLanguageParser.ExpressionsContext ctx) {
        stack.add(getFromStack(ExpressionDto.class, (ctx.getChildCount() + 1) / 2));
    }

    @Override
    public void exitStringExpression(QueryLanguageParser.StringExpressionContext ctx) {
        final String expression;
        if (ctx.getChildCount() == 3) {
            expression = ctx.getChild(0).getText() + " AS " + ctx.getChild(2).getText();
        } else {
            expression = ctx.getText();
        }
        stack.add(new StringExpressionDto(expression));
    }

    @Override
    public void exitFilterOr(QueryLanguageParser.FilterOrContext ctx) {
        stack.add(new OrDto(getFromStack(FilterDto.class, (ctx.getChildCount() + 1) / 2)));
    }

    @Override
    public void exitFilterAnd(QueryLanguageParser.FilterAndContext ctx) {
        stack.add(new AndDto(getFromStack(FilterDto.class, (ctx.getChildCount() + 1) / 2)));
    }

    @Override
    public void exitFilterNot(QueryLanguageParser.FilterNotContext ctx) {
        stack.add(new NotDto((FilterDto) stack.pop()));
    }

    @Override
    public void exitFilterStringExpression(QueryLanguageParser.FilterStringExpressionContext ctx) {
        stack.add(new StringExpressionDto(ctx.getText()));
    }

    @Override
    public void exitFilterTuple(QueryLanguageParser.FilterTupleContext ctx) {
        stack.add(new TupleDto((List<ExpressionDto>) stack.pop()));
    }

    @Override
    public void exitUnaryFilter(QueryLanguageParser.UnaryFilterContext ctx) {
        final UnaryComparisonOperator operator = UnaryComparisonOperator.valueOf(replaceSpaces(ctx.getChild(1).getText()));
        stack.add(new UnaryComparisonFilterDto((ExpressionDto) stack.pop(), operator));
    }

    @Override
    public void exitBinaryFilter(QueryLanguageParser.BinaryFilterContext ctx) {
        final BinaryComparisonFilterDto filter = new BinaryComparisonFilterDto();
        filter.setRight((ExpressionDto) stack.pop());
        filter.setOperator(binaryOperatorOf(ctx.getChild(1).getText()));
        filter.setLeft((ExpressionDto) stack.pop());
        stack.add(filter);
    }

    @Override
    public void exitSort(QueryLanguageParser.SortContext ctx) {
        final SortDirection direction = ctx.getChildCount() < 2 ? SortDirection.ASC : SortDirection.valueOf(ctx.getChild(1).getText().toUpperCase());
        stack.add(new SortDto((ExpressionDto) stack.pop(), direction));
    }

    @Override
    public void exitSorts(QueryLanguageParser.SortsContext ctx) {
        stack.add(getFromStack(SortDto.class, (ctx.getChildCount() + 1) / 2));
    }

    @Override
    public void exitLimitBy(QueryLanguageParser.LimitByContext ctx) {
        stack.add(new LimitByDto((List<ExpressionDto>) stack.pop(), Integer.parseInt(ctx.getChild(1).getText())));
    }

    private String replaceSpaces(final String text) {
        return text.replaceAll("\\s+", "_").toUpperCase();
    }

    private BinaryComparisonOperator binaryOperatorOf(final String text) {
        return switch (replaceSpaces(text)) {
            case "<" -> BinaryComparisonOperator.LESS;
            case "<=" -> BinaryComparisonOperator.LESS_OR_EQUALS;
            case ">" -> BinaryComparisonOperator.GREATER;
            case ">=" -> BinaryComparisonOperator.GREATER_OR_EQUALS;
            case "=", "==" -> BinaryComparisonOperator.EQUALS;
            case "<>", "!=" -> BinaryComparisonOperator.NOT_EQUALS;
            case "IN" -> BinaryComparisonOperator.IN;
            case "NOT_IN" -> BinaryComparisonOperator.NOT_IN;
            case "LIKE" -> BinaryComparisonOperator.LIKE;
            case "NOT_LIKE" -> BinaryComparisonOperator.NOT_LIKE;
            default -> throw new NotImplementedException(text);
        };
    }

    private <T> List<T> getFromStack(final Class<T> clazz, final int cnt) {
        final List<T> result = new ArrayList<>(cnt);
        for (int i = 0; i < cnt; ++i) {
            result.add(clazz.cast(stack.pop()));
        }
        return Lists.reverse(result);
    }
}
