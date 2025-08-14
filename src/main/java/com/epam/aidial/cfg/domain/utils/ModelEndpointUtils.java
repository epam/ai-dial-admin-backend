package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ModelType;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ModelEndpointUtils {

    private static final Pattern CHAT_MODEL_ENDPOINT_PATTERN = Pattern.compile("^(https?://.+?)(?:/([^/]+))?/chat/completions$");
    private static final Pattern NON_CHAT_MODEL_ENDPOINT_PATTERN = Pattern.compile("^(https?://.+?)(?:/([^/]+))?/embeddings$");

    private static final Map<Boolean, Pair<String, Pattern>> MODEL_PATTERN_MAP = Map.of(
            true, Pair.of("chat/completions", CHAT_MODEL_ENDPOINT_PATTERN),
            false, Pair.of("embeddings", NON_CHAT_MODEL_ENDPOINT_PATTERN)
    );

    public String createEndpoint(Model model) {
        var adapter = model.getAdapter();
        if (adapter == null) {
            return null;
        }
        String baseEndpoint = adapter.getBaseEndpoint();
        String endpointDeploymentName = model.getEndpointDeploymentName();
        return createEndpoint(baseEndpoint, endpointDeploymentName, model.getType());
    }

    private String createEndpoint(String baseEndpoint, String endpointDeploymentName, ModelType type) {
        String suffix = modelPath(endpointDeploymentName, type);
        return Strings.CS.appendIfMissing(baseEndpoint, "/") + suffix;
    }

    public ModelEndpointComponents parseModelEndpoint(String modelEndpoint, com.epam.aidial.core.config.ModelType type) {
        boolean isChat = isChat(type);
        String endpointEnding = MODEL_PATTERN_MAP.get(isChat).getLeft();
        boolean isDirectOpenAiEndpoint = modelEndpoint.endsWith("/v1/" + endpointEnding);

        if (isDirectOpenAiEndpoint) {
            String adapterEndpoint = Strings.CS.removeEnd(modelEndpoint, endpointEnding);
            return new ModelEndpointComponents(adapterEndpoint, null);
        }

        Pattern pattern = MODEL_PATTERN_MAP.get(isChat).getRight();
        Matcher matcher = pattern.matcher(modelEndpoint);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unable to extract adapter endpoint and endpoint deployment name "
                    + "from invalid model endpoint: " + modelEndpoint);
        }

        return new ModelEndpointComponents(matcher.group(1) + "/", matcher.group(2));
    }

    private boolean isChat(ModelType type) {
        return type == ModelType.CHAT || type == null;
    }

    private boolean isChat(com.epam.aidial.core.config.ModelType type) {
        return type == com.epam.aidial.core.config.ModelType.CHAT || type == null;
    }

    private String getEndpointByType(ModelType type) {
        return getEndpointByType(isChat(type));
    }

    private String getEndpointByType(boolean chat) {
        return MODEL_PATTERN_MAP.get(chat).getLeft();
    }

    private String modelPath(String endpointDeploymentName, ModelType type) {
        return StringUtils.isNotBlank(endpointDeploymentName)
                ? endpointDeploymentName + "/" + getEndpointByType(type)
                : getEndpointByType(type);
    }

    public record ModelEndpointComponents(String adapterEndpoint, @Nullable String endpointDeploymentName) {
    }
}
