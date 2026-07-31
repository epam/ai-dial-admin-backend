package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.value.LocalizedValueComparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@RequiredArgsConstructor
@Component
public class InterceptorComparator implements Comparator<Interceptor> {

    private final LocalizedValueComparator localizedValueComparator;

    @Override
    public int compare(Interceptor left, Interceptor right) {
        return Comparator
                .comparing(Interceptor::getDisplayName, localizedValueComparator)
                .thenComparing(Interceptor::getName)
                .compare(left, right);
    }
}
