package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.source.ModelSourceDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Created by Aliaksei Kurnosau on 9/11/24.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    @Pattern(
            regexp = "^(https://.+?/chat/completions|http://.+?/chat/completions)?$",
            message = "Must start 'http://' or 'https://' and end '/chat/completions'"
    )
    private String endpoint;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private String reference;
    private Boolean forwardAuthToken;
    private FeaturesDto features = new FeaturesDto();
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private List<String> topics;
    @Positive(message = "Max retry attempts should be greater than 0")
    private Integer maxRetryAttempts;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> dependencies;

    private ModelTypeDto type;
    private String tokenizerModel;
    private TokenLimitsDto limits;
    private PricingDto pricing;
    private List<UpstreamDto> upstreams = List.of();
    private String overrideName;
    private List<String> fieldsHashingOrder;
    private ModelSourceDto source;

}