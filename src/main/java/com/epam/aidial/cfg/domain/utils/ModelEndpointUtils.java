package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ModelType;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ModelEndpointUtils {

    private static final Pattern CHAT_MODEL_ENDPOINT_PATTERN = Pattern.compile("^(.*/)(.*?)/chat/completions$");
    private static final Pattern NON_CHAT_MODEL_ENDPOINT_PATTERN = Pattern.compile("^(.*/)(.*?)/embeddings$");

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
        String modelAlias = model.getAlias();
        return createEndpoint(baseEndpoint, modelAlias, model.getType());
    }

    private String createEndpoint(String baseEndpoint, String modelAlias, ModelType type) {
        String suffix = modelPath(modelAlias, type);
        return Strings.CS.appendIfMissing(baseEndpoint, "/") + suffix;
    }

    public String extractAdapterEndpoint(String modelEndpoint, com.epam.aidial.core.config.ModelType type) {
        return parseModelEndpoint(modelEndpoint, type, "adapter endpoint", 1);
    }

    public String extractModelAlias(String modelEndpoint, com.epam.aidial.core.config.ModelType type) {
        return parseModelEndpoint(modelEndpoint, type, "model alias", 2);
    }

    private String parseModelEndpoint(String modelEndpoint,
                                      com.epam.aidial.core.config.ModelType type,
                                      String parsingSubject,
                                      int group) {
        boolean isChat = isChat(type);
        Pattern pattern = MODEL_PATTERN_MAP.get(isChat).getRight();
        Matcher matcher = pattern.matcher(modelEndpoint);

        if (!matcher.matches()) {
            String modelEndpointPattern = "<adapter_base_endpoint>/any_string/" + getEndpointByType(isChat);
            throw new IllegalArgumentException("Unable to extract " + parsingSubject + " from invalid model endpoint: "
                    + modelEndpoint + ". Model endpoint must satisfy the following pattern: " + modelEndpointPattern);
        }

        return matcher.group(group);
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

    private String modelPath(String modelAlias, ModelType type) {
        return modelAlias != null
                ? modelAlias + "/" + getEndpointByType(type)
                : getEndpointByType(type);
    }
}
