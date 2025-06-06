parser grammar QueryLanguageParser;

options { tokenVocab=QueryLanguageLexer; }

root: union EOF;

union: query (UNION_ALL query)*;

query:
    SELECT DISTINCT? expressions
    FROM from
    join?
    (PRESCALE filter_or)?
    (WHERE filter_or)?
    (GROUP_BY expressions)? (WITH_TOTALS)?
    (HAVING filter_or)?
    (ORDER_BY sorts)?
    limitBy?
    (LIMIT decimal_literal (',' decimal_literal)?)?;

from: ('(' union ')' | table );

table: IDENTIFIER_LITERAL ('.' IDENTIFIER_LITERAL)*;

join: (ANY | ALL) (INNER | LEFT) JOIN from ON ( '(' expressions ')' EQUAL_OPERATOR '(' expressions ')' | expression_root EQUAL_OPERATOR expression_root);

filter_or
    : filter_and (OR_OPERATOR filter_and)+      #filterOr
    | filter_and                                #dummyFilterOr
    ;

filter_and
    : filter_not (AND_OPERATOR filter_not)+   #filterAnd
    | filter_not                              #dummyFilterAnd
    ;

filter_not
    : filter                      #dummyFilter
    | NOT_OPERATOR filter         #filterNot
    ;

expression_or_completable
    : '(' union ')'                                                            #filterSubquery
    | '(' expressions ')'                                                      #filterTuple
    | additive_expression                                                      #filterStringExpression
    ;

filter
    : '(' filter_or ')'                                                         #nestedFilter
    | expression_or_completable comparison_operator expression_or_completable   #binaryFilter
    | expression_or_completable unary_comparison_operator                       #unaryFilter
    ;

sorts: sort (',' sort)*;

sort: expression_root (ASC | DESC)?;

limitBy: LIMIT decimal_literal BY expressions;

expressions: expression_root (',' expression_root)*;

expression_root
    : expression (AS alias)?             #stringExpression
    | '(' union ')'                      #subquery
    ;

expression
   : logical_or_expression                                                                              #logicalExpression
   | logical_or_expression QUESTION_SYMBOL logical_or_expression COLON_SYMBOL logical_or_expression     #ternaryOperator
   ;

expression_atom
    : constant                                               #constantExpression
   | MINUS_OPERATOR constant                                 #unaryMinusOperator
   | PLUS_OPERATOR constant                                  #unaryPlusOperator
   | function_call                                           #functionCall
   | column_name                                             #columnNameExpression
   | MINUS_OPERATOR '(' expression ')'                       #negateOperator
   | '(' expression ')'                                      #nestedExpression
   | '(' expressions ')'                                     #tuple
   ;

logical_or_expression
    : logical_and_expression                                        #logicalDummyOrExpression
   | logical_or_expression OR_OPERATOR logical_and_expression       #logicalOrExpression
   ;

logical_and_expression
    : logical_not_expression                                        #logicalDummyAndExpression
   | logical_and_expression AND_OPERATOR logical_not_expression     #logicalAndExpression
   ;

logical_not_expression
    : comparison_expression                                         #logicalDummyNotExpression
   | NOT_OPERATOR comparison_expression                             #logicalNotExpression
   ;

comparison_expression
    : additive_expression                                           #comparisonDummyExpression
   | additive_expression comparison_operator additive_expression    #comparisonExpression
   | additive_expression unary_comparison_operator                  #unaryComparisonExpression
   ;

unary_comparison_operator:
    IS_NULL | IS_NOT_NULL;

comparison_operator:
    EQUAL_OPERATOR
    | NOT_EQUAL_OPERATOR
    | LESS_OPERATOR
    | GREATER_OPERATOR
    | LESS_OR_EQUAL_OPERATOR
    | GREATER_OR_EQUAL_OPERATOR
    | IN
    | NOT_IN
    | LIKE
    | NOT_LIKE
    ;

additive_expression
    : multiplicative_expression                                       #additiveDummyExpression
   | additive_expression additive_operator multiplicative_expression  #additiveExpression
   ;

additive_operator:
    PLUS_OPERATOR | MINUS_OPERATOR;

multiplicative_expression
    : expression_atom                                                       #multiplicativeDummyExpression
   | multiplicative_expression multiplicative_operator expression_atom      #multiplicativeExpression
   ;

multiplicative_operator:
    MULTIPLY_OPERATOR | DIVIDE_OPERATOR | MODULO_OPERATOR;

//    Functions

function:
    IDENTIFIER_LITERAL | ANY;

function_call
    : function function_params function_args;

function_params
    : ('(' function_param (',' function_param)* ')')?;

function_param
    : constant;

function_args
    : '(' (function_arg (',' function_arg)*)? ')';

function_arg
    : expression;

//    Common Clauses

alias
    : IDENTIFIER_LITERAL;

column_name
    : IDENTIFIER_LITERAL;

//    Literals

decimal_literal
    : DECIMAL_LITERAL;

string_literal
    : STRING_LITERAL+;

boolean_literal
    : TRUE | FALSE;

hexadecimal_literal
    : HEXADECIMAL_LITERAL;

real_literal
    : REAL_LITERAL      #realNumber
   | NAN                #nan
   | INF                #inf
   ;

number_literal
    : decimal_literal | hexadecimal_literal | real_literal;

null_literal: NULL;

binary_literal: HEX_STRING;

constant
    : number_literal | string_literal | boolean_literal | binary_literal | null_literal;
