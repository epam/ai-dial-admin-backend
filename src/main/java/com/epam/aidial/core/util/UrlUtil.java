package com.epam.aidial.core.util;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.net.PercentCodec;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

@UtilityClass
public class UrlUtil {

    private static final PercentCodec DECODER = new PercentCodec();

    private static final Escaper ENCODER = UrlEscapers.urlPathSegmentEscaper();

    @SneakyThrows
    public String encodePathSegment(String segment) {
        return ENCODER.escape(segment);
    }

    public String encodePath(String path) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < path.length(); i++) {
            int index = path.indexOf('/', i);
            if (index == -1) {
                builder.append(encodePathSegment(path.substring(i)));
                break;
            }

            builder.append(encodePathSegment(path.substring(i, index)));
            builder.append("/");
            i = index;
        }

        return builder.toString();
    }

    public String decodePath(String path) {
        return decodePath(path, true);
    }

    @SneakyThrows
    public String decodePath(String path, boolean checkUri) {
        if (checkUri) {
            try {
                URI uri = new URI(path);
                if (uri.getRawFragment() != null || uri.getRawQuery() != null) {
                    throw new IllegalArgumentException("Wrong path provided " + path);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return new String(DECODER.decode(path.getBytes(Charset.defaultCharset())));
    }

}