package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.common.deserializer.CommonFilterDeserializer;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.FromDto;
import com.epam.aidial.ql.dto.SortDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import com.epam.aidial.ql.dto.filters.OrDto;
import com.epam.aidial.ql.dto.filters.UnaryComparisonFilterDto;
import tools.jackson.core.Version;
import tools.jackson.databind.module.SimpleModule;

public class QueryLanguageModule extends SimpleModule {
    public QueryLanguageModule() {
        super("QueryLanguageModule", Version.unknownVersion());
        addDeserializer(ExpressionDto.class, new ExpressionDeserializer());
        addDeserializer(FilterDto.class, new CommonFilterDeserializer<>(
            UnaryComparisonFilterDto::new,
            BinaryComparisonFilterDto::new,
            AndDto.class,
            OrDto.class,
            NotDto.class,
            ExpressionDto.class
        ));
        addDeserializer(NotDto.class, new NotDeserializer());
        addDeserializer(CompletableDto.class, new CompletableDeserializer());
        addDeserializer(FromDto.class, new FromDeserializer());
        addDeserializer(SortDto.class, new SortDeserializer());
    }
}
