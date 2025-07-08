package com.epam.aidial.cfg.web.facade.mapper;

import org.mapstruct.Named;

import java.time.Instant;

/**
 * Utility mapper for converting between Instant and Long.
 */
public class InstantMapper {

    /**
     * Converts an Instant to a Long representing epoch milliseconds.
     *
     * @param instant the Instant to convert
     * @return the epoch milliseconds as a Long, or null if the input is null
     */
    @Named("instantToLong")
    public static Long mapInstantToLong(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    /**
     * Converts a Long representing epoch milliseconds to an Instant.
     *
     * @param epochMilli the epoch milliseconds to convert
     * @return the Instant, or null if the input is null
     */
    @Named("longToInstant")
    public static Instant mapLongToInstant(Long epochMilli) {
        return epochMilli != null ? Instant.ofEpochMilli(epochMilli) : null;
    }

    private InstantMapper() {
        // Utility class, no instances needed
    }
}