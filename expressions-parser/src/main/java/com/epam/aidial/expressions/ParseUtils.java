package com.epam.aidial.expressions;

import com.epam.aidial.expressions.impl.ColumnsListenerImpl;
import com.epam.aidial.expressions.impl.ErrorListener;
import com.epam.aidial.expressions.impl.ExpressionParserContext;
import com.epam.aidial.expressions.parser.ExpressionsLexer;
import com.epam.aidial.expressions.parser.ExpressionsParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseUtils {
    public static ValidRequestedExpression parseExpression(final String expression,
                                                           final FunctionsDatasource functionsDatasource,
                                                           final Map<String, ? extends Column> availableColumns) {
        return parseExpression(expression, functionsDatasource, availableColumns, false);
    }

    public static ValidRequestedExpression parseExpression(final String expression,
                                                           final FunctionsDatasource functionsDatasource,
                                                           final Map<String, ? extends Column> availableColumns,
                                                           final boolean allowAlias) {
        return (ValidRequestedExpression) new ExpressionParserContext(availableColumns, functionsDatasource, allowAlias, true, Collections.singletonList(expression)).parse().getLeft().get(0);
    }

    public static List<ValidRequestedExpression> parseExpressions(@Nullable final List<String> expressions,
                                                                  final FunctionsDatasource functionsDatasource,
                                                                  final Map<String, ? extends Column> availableColumns) {
        if (expressions == null) {
            return Collections.emptyList();
        }
        return (List<ValidRequestedExpression>) ((List) new ExpressionParserContext(availableColumns, functionsDatasource, false, true, expressions).parse().getLeft());
    }

    public static Pair<List<ValidRequestedExpression>, Map<String, ? extends Column>> parseExpressions(@Nullable final List<String> expressions,
                                                                                                       final FunctionsDatasource functionsDatasource,
                                                                                                       final Map<String, ? extends Column> availableColumns,
                                                                                                       final boolean allowAlias) {
        if (expressions == null) {
            return Pair.of(Collections.emptyList(), availableColumns);
        }
        return (Pair<List<ValidRequestedExpression>, Map<String, ? extends Column>>) (Pair) new ExpressionParserContext(availableColumns, functionsDatasource, allowAlias, true, expressions).parse();
    }

    public static Pair<List<RequestedExpression>, Map<String, ? extends Column>> parseExpressions(@Nullable final List<String> expressions,
                                                                                                  final FunctionsDatasource functionsDatasource,
                                                                                                  final Map<String, ? extends Column> availableColumns,
                                                                                                  final boolean allowAlias,
                                                                                                  final boolean stopOnError) {
        if (expressions == null) {
            return Pair.of(Collections.emptyList(), availableColumns);
        }
        return new ExpressionParserContext(availableColumns, functionsDatasource, allowAlias, stopOnError, expressions).parse();
    }

    public static Set<String> findDependencies(String expression) {
        CharStream input = null;
        try {
            input = new ANTLRInputStream(new StringReader(expression));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        final ExpressionsLexer lexer = new ExpressionsLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final ExpressionsParser parser = new ExpressionsParser(tokens);
        parser.addErrorListener(new ErrorListener());
        final ParseTree tree = parser.root();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final ColumnsListenerImpl visitor = new ColumnsListenerImpl();
        walker.walk(visitor, tree);
        if (visitor.getNumberOfErrors() != 0)
            throw new IllegalStateException(String.format("Expression '%s' is invalid", expression));
        return visitor.getDependencies();
    }
}
