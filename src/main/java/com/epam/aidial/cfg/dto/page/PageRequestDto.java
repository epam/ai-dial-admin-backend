package com.epam.aidial.cfg.dto.page;

import com.epam.aidial.cfg.dto.page.filter.FilterDto;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PageRequestDto {

    @PositiveOrZero
    private int pageNumber = 0;
    @Min(1)
    @Max(1000)
    @JsonSetter(nulls = Nulls.SKIP)
    private int pageSize = 20;
    private List<SortDto> sorts = new ArrayList<>();
    private List<FilterDto> filters = new ArrayList<>();
}
