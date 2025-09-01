package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.core.config.ModelType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ModelEndpointUtils {

    private static final Pattern CHAT_MODEL_ENDPOINT_PATTERN = Pattern.compile("^(https?://.+?)(?:/([^/]+))?/chat/completions$");
    private static final Pattern NON_CHAT_MODEL_ENDPOINT_PATTERN = Pattern.compile("^(https?://.+?)(?:/([^/]+))?/embeddings$");

    private static final Map<Boolean, Pair<String, Pattern>> MODEL_PATTERN_MAP = Map.of(
            true, Pair.of("chat/completions", CHAT_MODEL_ENDPOINT_PATTERN),
            false, Pair.of("embeddings", NON_CHAT_MODEL_ENDPOINT_PATTERN)
    );

    public ModelEndpointComponents parseModelEndpoint(String modelEndpoint, ModelType type) {
        boolean isChat = isChat(type);
        String endpointEnding = MODEL_PATTERN_MAP.get(isChat).getLeft();
        boolean isDirectOpenAiEndpoint = modelEndpoint.endsWith("/v1/" + endpointEnding);

        if (isDirectOpenAiEndpoint) {
            String adapterEndpoint = Strings.CS.removeEnd(modelEndpoint, endpointEnding);
            return new ModelEndpointComponents(adapterEndpoint, endpointEnding);
        }

        Pattern pattern = MODEL_PATTERN_MAP.get(isChat).getRight();
        Matcher matcher = pattern.matcher(modelEndpoint);

        if (!matcher.matches()) {
            log.info("Unable to extract adapter endpoint and completion endpoint path "
                    + "from model endpoint: " + modelEndpoint);
            return null;
        }

        return new ModelEndpointComponents(getValueFromMatcherGroup(matcher, 1), getValueFromMatcherGroup(matcher, 2) + endpointEnding);
    }

    public boolean isChat(com.epam.aidial.cfg.domain.model.ModelType type) {
        return type == com.epam.aidial.cfg.domain.model.ModelType.CHAT || type == null;
    }

    private boolean isChat(ModelType type) {
        return type == ModelType.CHAT || type == null;
    }

    private static String getValueFromMatcherGroup(Matcher matcher, int group) {
        return Optional.ofNullable(matcher.group(group)).orElse(StringUtils.EMPTY) + "/";
    }

    public static String concatEndpointAndPath(String baseEndpoint, String endpointPath) {
        if (baseEndpoint == null) {
            return endpointPath;
        }
        if (endpointPath == null) {
            return baseEndpoint;
        }

        String cleanedBase = baseEndpoint.endsWith("/") ? baseEndpoint.substring(0, baseEndpoint.length() - 1) : baseEndpoint;
        String cleanedPath = endpointPath.startsWith("/") ? endpointPath.substring(1) : endpointPath;

        return cleanedBase + "/" + cleanedPath;
    }

    public record ModelEndpointComponents(String adapterEndpoint, String completionEndpointPath) {
    }
}
