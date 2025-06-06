package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.parser.ExpressionsParser;
import com.epam.aidial.expressions.parser.ExpressionsParserBaseListener;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.HashSet;
import java.util.Set;

public class ColumnsListenerImpl extends ExpressionsParserBaseListener {
    private Set<String> dependencies = new HashSet<>();
    private int numberOfErrors = 0;

    @Override
    public void visitErrorNode(ErrorNode node) {
        ++numberOfErrors;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    @Override
    public void enterColumnNameExpression(ExpressionsParser.ColumnNameExpressionContext ctx) {
        dependencies.add(ctx.getText());
    }
}
