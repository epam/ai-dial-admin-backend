lexer grammar ExpressionsLexer;

channels { ERRORCHANNEL }

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

// START GENERATED CODE

FUNCTION_LITERAL:
'SHA1' |
'erfc' |
'upper' |
'toFixedString' |
'UUIDNumToString' |
'toStartOfHour' |
'toRelativeYearNum' |
'rand' |
'formatReadableSize' |
'bar' |
'toRelativeHourNum' |
'toStartOfDay' |
'sqrt' |
'hex' |
'toStartOfMinute' |
'toStartOfFiveMinute' |
'exp' |
'if' |
'greater' |
'intervalScaleEnd' |
'customIntervalScaleNumber' |
'lessOrEquals' |
'toInt32' |
'lower' |
'toStartOfMonth' |
'concat' |
'ceil' |
'cityHash64' |
'blockSize' |
'reinterpretAsUInt32' |
'toFloat64' |
'reinterpretAsDateTime' |
'intHash32' |
'appendTrailingCharIfAbsent' |
'bitXor' |
'reinterpretAsDate' |
'exp10' |
'toStringCutToZero' |
'positionUTF8' |
'toStartOfQuarter' |
'modulo' |
'customIntervalScaleStart' |
'hostName' |
'minus' |
'IPv4NumToString' |
'halfMD5' |
'lengthUTF8' |
'rowNumberInAllBlocks' |
'log' |
'log10' |
'unhex' |
'timeSlot' |
'bitOr' |
'toUInt64OrZero' |
'least' |
'customOrdinalScaleStart' |
'toInt64OrZero' |
'isFinite' |
'replaceRegexpOne' |
'intDiv' |
'toRelativeMonthNum' |
'toMinute' |
'toUInt32' |
'isInfinite' |
'intervalScaleRoundEnd' |
'reinterpretAsString' |
'reinterpretAsInt64' |
'erf' |
'visitParamExtractInt' |
'ordinalScaleEnd' |
'pow' |
'xor' |
'lowerUTF8' |
'log2' |
'visitParamExtractFloat' |
'SHA224' |
'toYear' |
'toFloat32OrZero' |
'e' |
'toEndOfInterval' |
'reverse' |
'UUIDStringToNum' |
'sipHash128' |
'abs' |
'IPv4StringToNum' |
'negate' |
'equals' |
'tgamma' |
'toString' |
'intervalScaleRoundNumber' |
'upperUTF8' |
'MD5' |
'visitParamExtractRaw' |
'intervalScaleNumber' |
'reinterpretAsFloat32' |
'materialize' |
'cos' |
'hasColumnInTable' |
'runningDifference' |
'isNaN' |
'atan' |
'empty' |
'toRelativeSecondNum' |
'replaceOne' |
'intHash64' |
'intervalScaleStart' |
'IPv6StringToNum' |
'toRelativeMinuteNum' |
'toUInt32OrZero' |
'exp2' |
'toRelativeWeekNum' |
'toDayOfMonth' |
'toDateTime' |
'toInt32OrZero' |
'replaceRegexpAll' |
'lgamma' |
'tan' |
'customOrdinalScaleNumber' |
'customOrdinalScaleEnd' |
'like' |
'bitShiftRight' |
'acos' |
'less' |
'visitParamExtractBool' |
'plus' |
'customIntervalScaleEnd' |
'rand64' |
'bitAnd' |
'extract' |
'toRelativeDayNum' |
'toDayOfWeek' |
'ordinalScaleStart' |
'substringUTF8' |
'IPv4NumToStringClassC' |
'IPv6NumToString' |
'position' |
'notEmpty' |
'intDivOrZero' |
'bitNot' |
'roundToExp2' |
'toStartOfInterval' |
'toInt64' |
'ordinalScaleNumber' |
'replaceAll' |
'greaterOrEquals' |
'substring' |
'cbrt' |
'toHour' |
'yesterday' |
'reinterpretAsUInt64' |
'not' |
'notLike' |
'toStartOfYear' |
'and' |
'today' |
'now' |
'ignore' |
'sin' |
'divide' |
'floor' |
'multiply' |
'reverseUTF8' |
'toTime' |
'greatest' |
'bitShiftLeft' |
'or' |
'reinterpretAsFloat64' |
'toDate' |
'visitParamExtractString' |
'match' |
'length' |
'toMonth' |
'toFloat32' |
'currentDatabase' |
'SHA256' |
'intervalScaleRoundStart' |
'toUInt64' |
'toSecond' |
'round' |
'visitParamHas' |
'toFloat64OrZero' |
'visitParamExtractUInt' |
'notEquals' |
'pi' |
'reinterpretAsInt32' |
'asin' |
'sipHash64' |
'toMonday'
;
AGGREGATION_FUNCTION_LITERAL:
'avgNotAbnormals' |
'avgIf' |
'corr' |
'stddevSampNotNans' |
'uniqHLL12If' |
'uniqCombined' |
'medianIf' |
'medianExactWeighted' |
'stddevSampNotInfs' |
'maxIf' |
'quantileNotNans' |
'quantile' |
'uniqIf' |
'medianDeterministic' |
'quantileNotInfs' |
'countIf' |
'anyLast' |
'countNotAbnormals' |
'medianNotInfs' |
'quantileIf' |
'anyIf' |
'medianNotAbnormals' |
'quantileExactWeightedIf' |
'medianExactNotNans' |
'quantileExactNotNans' |
'corrIf' |
'uniqCombinedIf' |
'count' |
'quantileTimingWeighted' |
'varSamp' |
'minNotAbnormals' |
'medianTimingWeightedIf' |
'sumNotAbnormals' |
'quantileExactNotInfs' |
'medianDeterministicIf' |
'uniqExact' |
'quantileTimingIf' |
'stddevPopNotNans' |
'median' |
'quantileExactNotAbnormals' |
'uniq' |
'stddevPopNotInfs' |
'minIf' |
'medianExactNotInfs' |
'medianTDigestIf' |
'sumIf' |
'quantileDeterministic' |
'medianTiming' |
'stddevSamp' |
'maxNotAbnormals' |
'sum' |
'varPopIf' |
'stddevPopNotAbnormals' |
'varPop' |
'medianExactIf' |
'covarSampIf' |
'avg' |
'min' |
'quantileExactWeighted' |
'quantileNotAbnormals' |
'minNotNans' |
'quantileTDigestIf' |
'uniqExactIf' |
'covarPop' |
'quantileExact' |
'sumNotNans' |
'stddevSampIf' |
'medianNotNans' |
'stddevPopIf' |
'minNotInfs' |
'sumNotInfs' |
'covarSamp' |
'anyLastIf' |
'countNotInfs' |
'max' |
'medianExactNotAbnormals' |
'countNotNans' |
'medianTimingIf' |
'uniqHLL12' |
'medianTimingWeighted' |
'quantileTDigest' |
'any' |
'covarPopIf' |
'maxNotNans' |
'medianExactWeightedIf' |
'quantileTiming' |
'medianExact' |
'maxNotInfs' |
'avgNotNans' |
'quantileTimingWeightedIf' |
'varSampIf' |
'stddevSampNotAbnormals' |
'quantileExactIf' |
'avgNotInfs' |
'quantileDeterministicIf' |
'stddevPop' |
'medianTDigest'
;
GROUP_FUNCTION_LITERAL:
'window'
;

// END GENERATED CODE

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
