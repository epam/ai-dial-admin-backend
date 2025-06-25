package com.epam.aidial.cfg.service.transfer;

import java.util.UUID;

public class ZipEntryNameNormalizer {

    /**
     * Re-writes {@code candidate} so that
     * <ul>
     *   <li>only letters, digits, '_', '-' and '.' remain,</li>
     *   <li>runs of consecutive dots are collapsed to a single dot,</li>
     *   <li>everything else becomes an underscore.</li>
     *   <li>A result that is empty, ".", or ".." triggers the configured fallback.</li>
     * </ul>
     */
    public static String normalise(String candidate) {

        if (candidate == null) {
            return UUID.randomUUID().toString();
        }

        StringBuilder out = new StringBuilder(candidate.length());
        boolean previousWasDot = false;

        for (int i = 0; i < candidate.length(); ) {
            int cp = candidate.codePointAt(i); // Read a full code point

            if (isAllowed(cp)) {
                if (cp == '.') {
                    /* skip this dot when the previous output char was also a dot */
                    if (!previousWasDot) {
                        out.append('.');
                        previousWasDot = true;
                    }
                } else {
                    out.appendCodePoint(cp);
                    previousWasDot = false;
                }
            } else {
                /* replace illegal characters with '_' */
                out.append('_');
                previousWasDot = false;
            }
            i += Character.charCount(cp); // Advance by the number of chars in the code point
        }

        String result = out.toString();
        // Fallback for problematic results
        if (result.isEmpty() || ".".equals(result) || "..".equals(result)) {
            result = UUID.randomUUID().toString();
        }
        return result;
    }

    private static boolean isAllowed(int cp) {
        return Character.isLetterOrDigit(cp)       // every Unicode letter/digit
                || cp == '_' || cp == '-' || cp == '.'; // keep these punctuation chars
    }

}
