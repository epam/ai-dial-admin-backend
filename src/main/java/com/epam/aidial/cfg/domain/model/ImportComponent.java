package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor()
@AllArgsConstructor// for tests
public class ImportComponent<T> {
    private ImportAction importAction;
    private T prev;
    private T next;
}
