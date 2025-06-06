parser grammar ExpressionsParser;

options { tokenVocab=ExpressionsLexer; }

root
    : expression EOF                #expressionWithoutAlias
    | expression AS alias EOF       #expressionWithAlias
    ;

expression
    : expression_atom                                                                                   #expressionAtom
   | logical_or_expression                                                                              #logicalExpression
   | logical_or_expression QUESTION_SYMBOL logical_or_expression COLON_SYMBOL logical_or_expression     #ternaryOperator
   ;

expression_atom
    : constant                                               #constantExpression
   | MINUS_OPERATOR constant                                 #unaryMinusOperator
   | PLUS_OPERATOR constant                                  #unaryPlusOperator
   | aggregation_function_call                               #aggregationFunctionCall
   | group_function_call                                     #groupFunctionCall
   | function_call                                           #functionCall
   | column_name                                             #columnNameExpression
   | MINUS_OPERATOR '(' expression ')'                       #negateOperator
   | '(' expression ')'                                      #nestedExpression
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
   ;

comparison_operator:
    EQUAL_OPERATOR | NOT_EQUAL_OPERATOR | LESS_OPERATOR | GREATER_OPERATOR | LESS_OR_EQUAL_OPERATOR | GREATER_OR_EQUAL_OPERATOR;

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
    FUNCTION_LITERAL;

function_call
    : function function_args;

aggregation_function
    : AGGREGATION_FUNCTION_LITERAL;

aggregation_function_call
    : aggregation_function function_args;

group_function
    : GROUP_FUNCTION_LITERAL;

group_function_call
    : group_function function_args;

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
