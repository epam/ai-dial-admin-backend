package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.CoreUpstreamAuthConfigWireSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Makes the Core object mapper write {@link CoreUpstreamAuthConfig} in Core's wire format, i.e. always as an object
 * with an explicit {@code headerName: null} for non-custom-header types, instead of the compact string form used by
 * the other mappers (export of the config to Core, storage), which Core also accepts.
 */
@JsonSerialize(using = CoreUpstreamAuthConfigWireSerializer.class)
public abstract class CoreUpstreamAuthConfigMixinForCoreObjectMapper {
}
