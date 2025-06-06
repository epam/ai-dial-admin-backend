package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.FunctionsDatasource;
import com.epam.aidial.expressions.RequestedExpression;
import com.epam.aidial.expressions.exceptions.ParseException;
import com.epam.aidial.expressions.parser.ExpressionsLexer;
import com.epam.aidial.expressions.parser.ExpressionsParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class ExpressionParserContext {
    private final Map<String, Column> columnByName;
    private final FunctionsDatasource functions;
    private final boolean allowAlias;
    private final boolean stopOnError;
    private final List<String> expressions;
    private final RequestedExpression[] result;

    private int id;

    public ExpressionParserContext(final Map<String, ? extends Column> columnByName,
                                   final FunctionsDatasource functions,
                                   final boolean allowAlias,
                                   final boolean stopOnError,
                                   final List<String> expressions) {
        this.columnByName = new HashMap<>(columnByName);
        this.functions = functions;
        this.allowAlias = allowAlias;
        this.stopOnError = stopOnError;
        this.expressions = expressions;
        result = new RequestedExpression[expressions.size()];
    }

    public boolean isAllowAlias() {
        return allowAlias;
    }

    public FunctionsDatasource getFunctions() {
        return functions;
    }

    public Column getColumn(final String name) {
        return columnByName.get(name);
    }

    public Collection<String> getColumnNames() {
        return columnByName.keySet();
    }

    public void addColumn(final Column column) {
        if (columnByName.containsKey(column.getName())) {
            throw new ParseException(String.format("Report already contain column '%s'", column.getName()));
        }
        columnByName.put(column.getName(), column);
    }

    public Pair<List<RequestedExpression>, Map<String, ? extends Column>> parse() {
        while (parseNext());
        return Pair.of(Arrays.asList(result), columnByName);
    }

    public boolean parseNext() {
        if (id == expressions.size()) {
            return false;
        }
        process(id++);
        return true;
    }

    private void process(final int id) {
        final String expression = expressions.get(id);
        if (expression == null) {
            throw new ParseException("Expression can not be null");
        }
        CharStream input = null;
        try {
            input = new ANTLRInputStream(new StringReader(expression));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        ParseTree tree = null;
        try {
            final ExpressionsLexer lexer = new ExpressionsLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final ExpressionsParser parser = new ExpressionsParser(tokens);
            parser.addErrorListener(new ErrorListener());
            tree = parser.root();
            final ParseTreeWalker walker = new ParseTreeWalker();
            final ExpressionsParserListenerImpl visitor = new ExpressionsParserListenerImpl(this);
            walker.walk(visitor, tree);
            if (visitor.getNumberOfErrors() != 0)
                throw new IllegalStateException(String.format("Expression '%s' is invalid", expression));

            result[id] = new ValidRequestedExpressionImpl(expression, visitor.getExpression());
        } catch (final ParseException e) {
            if (stopOnError) {
                throw e;
            } else {
                final String alias = tree == null || tree.getChild(2) == null ? expression : tree.getChild(2).getText();
                result[id] = new InvalidRequestedExpressionImpl(expression, alias, e.getMessage(), e.getErrorType());
            }
        }
    }
}
