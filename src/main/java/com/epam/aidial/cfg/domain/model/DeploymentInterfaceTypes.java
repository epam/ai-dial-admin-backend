package com.epam.aidial.cfg.domain.model;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Interface types supported by DIAL Core 0.46.0 as keys of the deployment {@code interfaces} map.
 * The allowed key set differs per deployment kind, mirroring DIAL Core semantics.
 */
@UtilityClass
public class DeploymentInterfaceTypes {

    public static final String OPENAI_CHAT_COMPLETIONS = "openaiChatCompletions";
    public static final String OPENAI_RESPONSES = "openaiResponses";
    public static final String ANTHROPIC_MESSAGES = "anthropicMessages";

    public static final Set<String> MODEL_INTERFACE_TYPES =
            Set.of(OPENAI_CHAT_COMPLETIONS, OPENAI_RESPONSES, ANTHROPIC_MESSAGES);
    public static final Set<String> APPLICATION_INTERFACE_TYPES = Set.of(OPENAI_CHAT_COMPLETIONS);
    public static final Set<String> INTERCEPTOR_INTERFACE_TYPES = Set.of(OPENAI_CHAT_COMPLETIONS);
}
