package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportComponentDto<T> {
    private ImportActionDto importAction;
    private T prev;
    private T next;
}
