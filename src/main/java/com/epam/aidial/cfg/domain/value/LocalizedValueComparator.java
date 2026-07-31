package com.epam.aidial.cfg.domain.value;

import com.epam.aidial.cfg.configuration.LocalizationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class LocalizedValueComparator
        implements Comparator<LocalizedValue> {

    private final LocalizationProperties localizationProperties;

    @Override
    public int compare(LocalizedValue left,
                       LocalizedValue right) {

        return Comparator
                .nullsFirst(String.CASE_INSENSITIVE_ORDER)
                .compare(
                        left == null ? null : left.resolve(null, localizationProperties.getLocale()),
                        right == null ? null : right.resolve(null, localizationProperties.getLocale()));
    }
}
