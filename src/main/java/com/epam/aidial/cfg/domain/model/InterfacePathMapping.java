package com.epam.aidial.cfg.domain.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mirrors DIAL Core's {@code com.epam.aidial.core.config.InterfacePathMapping} (Core 0.48.0,
 * commit 6ed0c9a6bfdeb2fe25036da4ee98b44117a88619). Each known {@code overridePaths} key names
 * exactly one interface type and says whether the operation carries an id for {@code {id}} to
 * render. A key this admin backend does not recognize is tolerated without validation, exactly as
 * Core itself tolerates it — the mapping set is owned by Core and may grow with a newer Core
 * release before this list is updated.
 */
@Getter
public enum InterfacePathMapping {

    POST_AZURE_OPENAI_CHAT_COMPLETIONS(
            "postAzureOpenaiChatCompletions", DeploymentInterfaceTypes.OPENAI_CHAT_COMPLETIONS, true),
    POST_AZURE_OPENAI_EMBEDDINGS(
            "postAzureOpenaiEmbeddings", DeploymentInterfaceTypes.OPENAI_EMBEDDINGS, true),

    POST_OPENAI_RESPONSES(
            "postOpenaiResponses", DeploymentInterfaceTypes.OPENAI_RESPONSES, false),
    GET_OPENAI_RESPONSES_BY_ID(
            "getOpenaiResponsesById", DeploymentInterfaceTypes.OPENAI_RESPONSES, true),
    DELETE_OPENAI_RESPONSES_BY_ID(
            "deleteOpenaiResponsesById", DeploymentInterfaceTypes.OPENAI_RESPONSES, true),
    POST_OPENAI_RESPONSES_CANCEL(
            "postOpenaiResponsesCancel", DeploymentInterfaceTypes.OPENAI_RESPONSES, true),

    POST_ANTHROPIC_MESSAGES(
            "postAnthropicMessages", DeploymentInterfaceTypes.ANTHROPIC_MESSAGES, false),
    POST_ANTHROPIC_MESSAGES_COUNT_TOKENS(
            "postAnthropicMessagesCountTokens", DeploymentInterfaceTypes.ANTHROPIC_MESSAGES, false);

    private static final Map<String, InterfacePathMapping> BY_VALUE = new HashMap<>();

    static {
        for (InterfacePathMapping mapping : values()) {
            BY_VALUE.put(mapping.value, mapping);
        }
    }

    private final String value;

    /**
     * The interface type whose {@code overridePaths} map this key is read from.
     */
    private final String interfaceType;

    /**
     * Whether the {@code {id}} template variable applies: the operation addresses an id to render.
     */
    private final boolean idApplicable;

    InterfacePathMapping(String value, String interfaceType, boolean idApplicable) {
        this.value = value;
        this.interfaceType = interfaceType;
        this.idApplicable = idApplicable;
    }

    /**
     * The mapping with this value, or {@code null} for one this admin backend does not know —
     * a config may name a key that only a newer Core release understands.
     */
    public static InterfacePathMapping find(String value) {
        return BY_VALUE.get(value);
    }
}
