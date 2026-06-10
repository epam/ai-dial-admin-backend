package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreFeatures {
    @JsonAlias({"rateEndpoint", "rate_endpoint"})
    private String rateEndpoint;
    @JsonAlias({"tokenizeEndpoint", "tokenize_endpoint"})
    private String tokenizeEndpoint;
    @JsonAlias({"truncatePromptEndpoint", "truncate_prompt_endpoint"})
    private String truncatePromptEndpoint;
    @JsonAlias({"configurationEndpoint", "configuration_endpoint"})
    private String configurationEndpoint;

    @JsonAlias({"systemPromptSupported", "system_prompt_supported"})
    private Boolean systemPromptSupported;
    @JsonAlias({"toolsSupported", "tools_supported"})
    private Boolean toolsSupported;
    @JsonAlias({"seedSupported", "seed_supported"})
    private Boolean seedSupported;

    @JsonAlias({"urlAttachmentsSupported", "url_attachments_supported"})
    private Boolean urlAttachmentsSupported;
    @JsonAlias({"folderAttachmentsSupported", "folder_attachments_supported"})
    private Boolean folderAttachmentsSupported;
    @JsonAlias({"allowResume", "allow_resume"})
    private Boolean allowResume;
    @JsonAlias({"accessibleByPerRequestKey", "accessible_by_per_request_key"})
    private Boolean accessibleByPerRequestKey;
    @JsonAlias({"contentPartsSupported", "content_parts_supported"})
    private Boolean contentPartsSupported;
    @JsonAlias({"temperatureSupported", "temperature_supported"})
    private Boolean temperatureSupported;

    @JsonAlias({"cacheSupported", "cache_supported"})
    private Boolean cacheSupported; // 0.26.0

    @JsonAlias({"autoCachingSupported", "auto_caching_supported"})
    private Boolean autoCachingSupported; // 0.26.0
    @JsonAlias({"consentRequired", "consent_required"})
    private Boolean consentRequired; // 0.27.0

    @JsonAlias({"parallelToolCallsSupported", "parallel_tool_calls_supported"})
    private Boolean parallelToolCallsSupported; // 0.29.0

    @JsonAlias({"assistantAttachmentsInRequestSupported", "assistant_attachments_in_request_supported"})
    private Boolean assistantAttachmentsInRequestSupported; //0.37.0

    @JsonAlias({"supportCommentInRateResponse", "support_comment_in_rate_response"})//0.38.0
    private Boolean supportCommentInRateResponse;

    @JsonAlias({"maxTokensSupported", "max_tokens_supported"})
    private Boolean maxTokensSupported; // 0.45.0

    @JsonAlias({"maxCompletionTokensSupported", "max_completion_tokens_supported"})
    private Boolean maxCompletionTokensSupported; // 0.45.0

    @JsonAlias({"customTemperatureSupported", "custom_temperature_supported"})
    private Boolean customTemperatureSupported; // 0.45.0

    @JsonAlias({"reasoningEffortsSupported", "reasoning_efforts_supported"})
    private Boolean reasoningEffortsSupported; // 0.45.0
}