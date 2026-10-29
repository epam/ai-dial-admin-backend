package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.UpstreamAuthConfig;
import com.epam.aidial.cfg.domain.model.UpstreamAuthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpstreamValidatorTest {

    private UpstreamValidator upstreamValidator;

    @BeforeEach
    void setUp() {
        upstreamValidator = new UpstreamValidator();
    }

    @Test
    void validateForRoutes_shouldPassWhenUpstreamsIsNull() {
        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(null, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldPassWhenUpstreamsIsEmpty() {
        // given
        List<Upstream> upstreams = new ArrayList<>();

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldPassWhenAuthTypeIsApiKeyWithoutKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.API_KEY));
        upstream.setKey(null);

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldPassWhenAuthTypeIsApiKeyWithKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.API_KEY));
        upstream.setKey("test-key");

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldPassWhenAuthTypeIsBearerWithKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.BEARER));
        upstream.setKey("test-key");

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldFailWhenAuthTypeIsBearerWithoutKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.BEARER));
        upstream.setKey(null);

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upstream with authType BEARER requires a key")
                .hasMessageContaining("upstreams[0]")
                .hasMessageContaining("Route: test-route");
    }

    @Test
    void validateForRoutes_shouldFailWhenAuthTypeIsBearerWithEmptyKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.BEARER));
        upstream.setKey("");

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upstream with authType BEARER requires a key")
                .hasMessageContaining("upstreams[0]")
                .hasMessageContaining("Route: test-route");
    }

    @Test
    void validateForRoutes_shouldFailWhenAuthTypeIsBearerWithBlankKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.BEARER));
        upstream.setKey("   ");

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upstream with authType BEARER requires a key")
                .hasMessageContaining("upstreams[0]")
                .hasMessageContaining("Route: test-route");
    }

    @Test
    void validateForRoutes_shouldReportCorrectIndexForSecondUpstream() {
        // given
        Upstream validUpstream = new Upstream();
        validUpstream.setEndpoint("http://localhost");
        validUpstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.API_KEY));
        validUpstream.setKey("test-key");

        Upstream invalidUpstream = new Upstream();
        invalidUpstream.setEndpoint("http://localhost2");
        invalidUpstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.BEARER));
        invalidUpstream.setKey(null);

        List<Upstream> upstreams = List.of(validUpstream, invalidUpstream);

        // when/then
        assertThatThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("upstreams[1]")
                .hasMessageContaining("Route: test-route");
    }

    @Test
    void validateForRoutes_shouldPassWithMultipleValidUpstreams() {
        // given
        Upstream apiKeyUpstream = new Upstream();
        apiKeyUpstream.setEndpoint("http://localhost");
        apiKeyUpstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.API_KEY));
        apiKeyUpstream.setKey(null);

        Upstream bearerUpstream = new Upstream();
        bearerUpstream.setEndpoint("http://localhost2");
        bearerUpstream.setAuthType(UpstreamAuthConfig.of(UpstreamAuthType.BEARER));
        bearerUpstream.setKey("bearer-token");

        List<Upstream> upstreams = List.of(apiKeyUpstream, bearerUpstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldPassWhenAuthTypeIsNullWithoutKey() {
        // given - authType defaults to API_KEY, but could be explicitly null
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(null);
        upstream.setKey(null);

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @Test
    void validateForRoutes_shouldPassWhenAuthTypeIsCustomHeaderWithKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.customHeader("X-Api-Token"));
        upstream.setKey("secret");

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateForRoutes_shouldFailWhenAuthTypeIsCustomHeaderWithoutKey(String key) {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(UpstreamAuthConfig.customHeader("X-Api-Token"));
        upstream.setKey(key);

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upstream with authType CUSTOM_HEADER requires a key")
                .hasMessageContaining("upstreams[0]")
                .hasMessageContaining("Route: test-route");
    }

    @Test
    void validateForRoutes_shouldPassWhenAuthTypeTypeIsNullWithoutKey() {
        // given
        Upstream upstream = new Upstream();
        upstream.setEndpoint("http://localhost");
        upstream.setAuthType(new UpstreamAuthConfig());
        upstream.setKey(null);

        List<Upstream> upstreams = List.of(upstream);

        // when/then
        assertThatNoException().isThrownBy(() ->
                upstreamValidator.validateForRoutes(upstreams, "test-route")
        );
    }
}
