package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.value.LocalizedValueComparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@RequiredArgsConstructor
@Component
public class ToolSetComparator implements Comparator<ToolSet> {

    private final LocalizedValueComparator localizedValueComparator;

    @Override
    public int compare(ToolSet left, ToolSet right) {
        return Comparator
                .comparing(ToolSet::getDisplayName, localizedValueComparator)
                .thenComparing(t -> t.getDeployment().getName())
                .compare(left, right);
    }
}
