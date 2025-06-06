package com.epam.aidial.expressions;

import org.apache.commons.lang3.text.WordUtils;

public class NameUtils {
    public static String generateName(String name) {
        return WordUtils.uncapitalize(WordUtils.capitalizeFully(name, '_').replaceAll("_", ""));
    }
}
