package com.epam.aidial.cfg.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@UtilityClass
public final class ResourceUtils {

    public static String readResource(String resource) {
        try (InputStream resourceAsStream = ResourceUtils.class.getResourceAsStream(resource)) {
            return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            String errorMessage = String.format("Can't get resource: %s", resource);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    public static InputStream getStream(String resource) {
        try (InputStream resourceAsStream = ResourceUtils.class.getResourceAsStream(resource)) {
            return IOUtils.toBufferedInputStream(resourceAsStream);
        } catch (Exception e) {
            String errorMessage = String.format("Can't get resource: %s", resource);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

}