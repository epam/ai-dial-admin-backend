lexer grammar QueryLanguageLexer;

channels { ERRORCHANNEL }

SPACE:                               [ \t\r\n]+    -> channel(HIDDEN);

SELECT:                              S E L E C T;
DISTINCT:                            D I S T I N C T;
FROM:                                F R O M;
ANY:                                 A N Y;
ALL:                                 A L L;
INNER:                               I N N E R;
LEFT:                                L E F T;
JOIN:                                J O I N;
PRESCALE:                            P R E S C A L E;
WHERE:                               W H E R E;
GROUP_BY:                            G R O U P SPACE B Y;
HAVING:                              H A V I N G;
ORDER_BY:                            O R D E R SPACE B Y;
WITH_TOTALS:                         W I T H SPACE T O T A L S;
ASC:                                 A S C;
DESC:                                D E S C;
UNION_ALL:                           U N I O N SPACE A L L;
LIMIT:                               L I M I T;
BY:                                  B Y;
ON:                                  O N;
IN:                                  I N;
NOT_IN:                              N O T SPACE I N;
IS_NULL:                             I S SPACE N U L L;
IS_NOT_NULL:                         I S SPACE N O T SPACE N U L L;
LIKE:                                L I K E;
NOT_LIKE:                            N O T SPACE L I K E;

// Keywords
// Common Keywords

AS:                                  A S;
FALSE:                               F A L S E;
TRUE:                                T R U E;
NAN:                                 N A N;
INF:                                 I N F | I N F I N I T Y;
NULL:                                N U L L;

// Operators. Arithmetics

MULTIPLY_OPERATOR:                   '*';
DIVIDE_OPERATOR:                     '/';
MODULO_OPERATOR:                     '%';
PLUS_OPERATOR:                       '+';
MINUS_OPERATOR:                      '-';

// Operators. Comparisons

EQUAL_OPERATOR:                      '==' | '=';
NOT_EQUAL_OPERATOR:                  '!=' | '<>';
LESS_OPERATOR:                       '<';
GREATER_OPERATOR:                    '>';
LESS_OR_EQUAL_OPERATOR:              '<=';
GREATER_OR_EQUAL_OPERATOR:           '>=';

// Operators. Logicals

NOT_OPERATOR:                        'NOT';
AND_OPERATOR:                        'AND';
OR_OPERATOR:                         'OR';

// Ternary operators

QUESTION_SYMBOL:                     '?';
COLON_SYMBOL:                        ':';

// Operators. Bit

BIT_NOT_OP:                          '~';
BIT_OR_OP:                           '|';
BIT_AND_OP:                          '&';
BIT_XOR_OP:                          '^';

// Constructors symbols

DOT:                                 '.';
LR_BRACKET:                          '(';
RR_BRACKET:                          ')';
COMMA:                               ',';
SEMI:                                ';';
AT_SIGN:                             '@';
SINGLE_QUOTE_SYMB:                   '\'';
DOUBLE_QUOTE_SYMB:                   '"';
REVERSE_QUOTE_SYMB:                  '`';

// Literal Primitives

IDENTIFIER_LITERAL:                  IDENTIFIER/* | BQUOTA_STRING*/;
STRING_LITERAL:                      DQUOTA_STRING | SQUOTA_STRING;
HEX_STRING:                          'X\'' HEX_DIGIT+ '\'';
DECIMAL_LITERAL:                     DEC_DIGIT+;
HEXADECIMAL_LITERAL:                 '0x' HEX_DIGIT+;

REAL_LITERAL:                        (DEC_DIGIT+)? '.' DEC_DIGIT+ |
                                        DEC_DIGIT+ '.' EXPONENT_NUM_PART |
                                        (DEC_DIGIT+)? '.' (DEC_DIGIT+ EXPONENT_NUM_PART) |
                                        DEC_DIGIT+ EXPONENT_NUM_PART
                                     ;
//NULL_SPEC_LITERAL:                   '\\' 'N';
//BIT_STRING:                          BIT_STRING_L;

// Fragments for Literal primitives

fragment EXPONENT_NUM_PART:          E (PLUS_OPERATOR|MINUS_OPERATOR)? DEC_DIGIT+;
fragment IDENTIFIER:                 [a-zA-Z_][0-9a-zA-Z_]*;
fragment DQUOTA_STRING:              '"' ( '\\'. | '""' | ~('"'| '\\') )* '"';
fragment SQUOTA_STRING:              '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\'';
//fragment BQUOTA_STRING:              '`' ( '\\'. | '``' | ~('`'|'\\'))* '`';
fragment HEX_DIGIT:                  [0-9A-Fa-f];
fragment DEC_DIGIT:                  [0-9];
//fragment BIT_STRING_L:               'b' '\'' [01]+ '\'';

// Letters

fragment A:                          [aA];
fragment B:                          [bB];
fragment C:                          [cC];
fragment D:                          [dD];
fragment E:                          [eE];
fragment F:                          [fF];
fragment G:                          [gG];
fragment H:                          [hH];
fragment I:                          [iI];
fragment J:                          [jJ];
fragment K:                          [kK];
fragment L:                          [lL];
fragment M:                          [mM];
fragment N:                          [nN];
fragment O:                          [oO];
fragment P:                          [pP];
fragment Q:                          [qQ];
fragment R:                          [rR];
fragment S:                          [sS];
fragment T:                          [tT];
fragment U:                          [uU];
fragment V:                          [vV];
fragment W:                          [wW];
fragment X:                          [xX];
fragment Y:                          [yY];
fragment Z:                          [zZ];

// Last tokens must generate Errors

ERROR_RECONGNIGION:                  .    -> channel(ERRORCHANNEL);
