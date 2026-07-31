package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.value.LocalizedValueComparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@RequiredArgsConstructor
@Component
public class ModelComparator implements Comparator<Model> {

    private final LocalizedValueComparator localizedValueComparator;

    @Override
    public int compare(Model left, Model right) {
        return Comparator
                .comparing(Model::getDisplayName, localizedValueComparator)
                .thenComparing(Model::getDisplayVersion,
                        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(t -> t.getDeployment().getName())
                .compare(left, right);
    }
}
