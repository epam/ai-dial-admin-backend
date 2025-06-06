package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class JsonTests {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new QueryLanguageModule());
    }

    @Test
    public void shouldParseFilters() throws Throwable {
        final FilterDto actual = objectMapper.readValue("""
                {
                  "$and": [
                    {
                      "$or": [
                        {
                          "$not": {
                            "$and": [
                              {
                                "$lt": {
                                  "left": "x",
                                  "right": 1
                                }
                              },
                              {
                                "$gt": {
                                  "left": "x",
                                  "right": 2
                                }
                              }
                            ]
                          }
                        },
                        {
                          "$in": {
                            "left": "x",
                            "right": [1, 2, 3]
                          }
                        }
                      ]
                    },
                    {
                      "$isNull": "y"
                    }
                  ]
                }""", FilterDto.class);
        final FilterDto expected = new AndDto(Arrays.asList(
                new OrDto(Arrays.asList(
                        new NotDto(new AndDto(Arrays.asList(
                                new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.LESS, new StringExpressionDto("1")),
                                new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.GREATER, new StringExpressionDto("2"))
                        ))),
                        new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.IN, new TupleDto(Arrays.asList(
                                new StringExpressionDto("1"),
                                new StringExpressionDto("2"),
                                new StringExpressionDto("3")
                        )))
                )),
                new UnaryComparisonFilterDto(new StringExpressionDto("y"), UnaryComparisonOperator.IS_NULL)
        ));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldParseExpressions() throws Throwable {
        final List<ExpressionDto> actual =
                objectMapper.readValue("[\"a\", \"'a'\", 1, [\"a\", \"b\"]]", new TypeReference<List<ExpressionDto>>(){});
        final List<ExpressionDto> expected = Arrays.asList(
                new StringExpressionDto("a"),
                new StringExpressionDto("'a'"),
                new StringExpressionDto("1"),
                new TupleDto(Arrays.asList(new StringExpressionDto("a"), new StringExpressionDto("b")))
        );
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAllQuery() throws Throwable {
        final CompletableDto actual = objectMapper.readValue("""
                {
                  "expressions": ["x", "y", "count()"],
                  "from": {
                    "unionAll": [
                      {
                        "expressions": ["x", "y"],
                        "from": "table1"
                      },
                      {
                        "expressions": ["a3", "b3"],
                        "from": "tableA",
                        "join": {
                          "strictness": "Any",
                          "type": "Inner",
                          "from": "tableB",
                          "left": ["a1", "a2"],
                          "right": ["b1", "b2"]
                        }
                      }
                    ]
                  },
                  "where": {
                    "$and": [
                      {
                        "$ne": {
                          "left": "x",
                          "right": 0
                        }
                      },
                      {
                        "$nin": {
                          "left": ["x", "y"],
                          "right": {
                            "distinct": true,
                            "expressions": ["a", "b"],
                            "from": "reportX"
                          }
                        }
                      },
                      {
                        "$isNotNull": "y"
                      }
                    ]
                  },
                  "groupBy": ["x", "y"],
                  "withTotals": true,
                  "having": {
                    "$gte": {
                      "left": "count()",
                      "right": "x"
                    }
                  },
                  "orderBy": [
                    {
                      "$desc": "count()"
                    },
                    {
                      "$asc": "x"
                    },
                    {
                      "$asc": "y"
                    }
                  ],
                  "limitBy": {
                    "expressions": ["x"],
                    "limit": 10
                  },
                  "offset": 10,
                  "limit": 10
                }""", CompletableDto.class);

        final QueryDto expected = new QueryDto();

        expected.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y"), new StringExpressionDto("count()")));

        final QueryDto union1 = new QueryDto();
        union1.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        union1.setFrom(new TableDto("table1"));

        final QueryDto union2 = new QueryDto();
        union2.setExpressions(Arrays.asList(new StringExpressionDto("a3"), new StringExpressionDto("b3")));
        union2.setFrom(new TableDto("tableA"));
        union2.setJoin(new JoinDto(
                JoinStrictness.ANY,
                JoinType.INNER,
                new TableDto("tableB"),
                new TupleDto(Arrays.asList(new StringExpressionDto("a1"), new StringExpressionDto("a2"))),
                new TupleDto(Arrays.asList(new StringExpressionDto("b1"), new StringExpressionDto("b2")))
        ));

        final UnionAllDto unionAll = new UnionAllDto(Arrays.asList(union1, union2));
        expected.setFrom(unionAll);

        final QueryDto subquery = new QueryDto();
        subquery.setDistinct(true);
        subquery.setExpressions(Arrays.asList(new StringExpressionDto("a"), new StringExpressionDto("b")));
        subquery.setFrom(new TableDto("reportX"));
        expected.setWhere(new AndDto(Arrays.asList(
                new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.NOT_EQUALS, new StringExpressionDto("0")),
                new BinaryComparisonFilterDto(
                        new TupleDto(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y"))),
                        BinaryComparisonOperator.NOT_IN,
                        subquery
                ),
                new UnaryComparisonFilterDto(new StringExpressionDto("y"), UnaryComparisonOperator.IS_NOT_NULL)
        )));

        expected.setGroupBy(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        expected.setWithTotals(true);
        expected.setHaving(new BinaryComparisonFilterDto(new StringExpressionDto("count()"), BinaryComparisonOperator.GREATER_OR_EQUALS, new StringExpressionDto("x")));
        expected.setOrderBy(Arrays.asList(
                new SortDto(new StringExpressionDto("count()"), SortDirection.DESC),
                new SortDto(new StringExpressionDto("x"), SortDirection.ASC),
                new SortDto(new StringExpressionDto("y"), SortDirection.ASC)
        ));
        expected.setLimitBy(new LimitByDto(List.of(new StringExpressionDto("x")), 10));
        expected.setOffset(10L);
        expected.setLimit(10L);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldParseTuple() throws Throwable {
        final TupleDto actual = objectMapper.readValue("[1, 2, 3, \"4\", \"string\"]", TupleDto.class);
        final TupleDto expected = new TupleDto(Arrays.asList(
                new StringExpressionDto("1"),
                new StringExpressionDto("2"),
                new StringExpressionDto("3"),
                new StringExpressionDto("4"),
                new StringExpressionDto("string")
        ));
        Assert.assertEquals(expected, actual);
    }
}
