package com.epam.aidial.ql.model.impl;


import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.UnionAll;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class UnionAllImpl implements UnionAll {
    @Singular
    private List<Completable> queries;
}
