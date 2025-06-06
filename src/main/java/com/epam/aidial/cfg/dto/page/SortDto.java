package com.epam.aidial.cfg.dto.page;

import com.epam.aidial.cfg.domain.model.page.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortDto {
    private String column;
    private SortDirection direction;
}


