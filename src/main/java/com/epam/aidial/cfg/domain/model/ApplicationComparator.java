package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.value.LocalizedValueComparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@RequiredArgsConstructor
@Component
public class ApplicationComparator implements Comparator<Application> {

    private final LocalizedValueComparator localizedValueComparator;

    @Override
    public int compare(Application left, Application right) {
        return Comparator
                .comparing(Application::getDisplayName, localizedValueComparator)
                .thenComparing(Application::getDisplayVersion,
                        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(t -> t.getDeployment().getName())
                .compare(left, right);
    }
}
