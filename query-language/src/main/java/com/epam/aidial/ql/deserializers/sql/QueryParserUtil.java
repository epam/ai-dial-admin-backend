package com.epam.aidial.ql.deserializers.sql;

import com.epam.aidial.expressions.impl.ErrorListener;
import com.epam.aidial.ql.deserializers.sql.gen.QueryLanguageLexer;
import com.epam.aidial.ql.deserializers.sql.gen.QueryLanguageParser;
import com.epam.aidial.ql.dto.CompletableDto;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.StringReader;

public class QueryParserUtil {
    public static CompletableDto parse(final String query) {
        CharStream input = null;
        try {
            input = new ANTLRInputStream(new StringReader(query));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        final QueryLanguageLexer lexer = new QueryLanguageLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final QueryLanguageParser parser = new QueryLanguageParser(tokens);
        parser.addErrorListener(new ErrorListener());
        final ParseTree tree = parser.root();
        final ParseTreeWalker walker = new ParseTreeWalker();
        final QueryLangageListenerImpl visitor = new QueryLangageListenerImpl();
        walker.walk(visitor, tree);
        if (visitor.getNumberOfErrors() != 0)
            throw new IllegalStateException(String.format("Query '%s' is invalid", query));
        return visitor.getCompletable();
    }
}
