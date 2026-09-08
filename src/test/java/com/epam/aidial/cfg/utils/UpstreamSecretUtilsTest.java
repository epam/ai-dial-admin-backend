package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.UpstreamInterface;
import com.epam.aidial.core.config.CoreUpstream;
import com.epam.aidial.core.config.CoreUpstreamInterface;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UpstreamSecretUtilsTest {

    @Test
    void testRemoveSecrets_StripsBothLevelsAndKeepsPublicFields() {
        // given
        UpstreamInterface upstreamInterface = new UpstreamInterface();
        upstreamInterface.setEndpoint("https://api.example.com/v1/messages");
        upstreamInterface.setKey("interface-key");
        upstreamInterface.setExtraData("interfaceExtraData");
        upstreamInterface.setSecretExtraData("interfaceSecretExtraData");

        Upstream upstream = new Upstream();
        upstream.setEndpoint("https://api.example.com");
        upstream.setKey("upstream-key");
        upstream.setExtraData("upstreamExtraData");
        upstream.setSecretExtraData("upstreamSecretExtraData");
        upstream.setInterfaces(Map.of("anthropicMessages", upstreamInterface));

        // when
        UpstreamSecretUtils.removeSecrets(List.of(upstream));

        // then
        Assertions.assertThat(upstream.getKey()).isNull();
        Assertions.assertThat(upstream.getSecretExtraData()).isNull();
        Assertions.assertThat(upstream.getEndpoint()).isEqualTo("https://api.example.com");
        Assertions.assertThat(upstream.getExtraData()).isEqualTo("upstreamExtraData");
        Assertions.assertThat(upstreamInterface.getKey()).isNull();
        Assertions.assertThat(upstreamInterface.getSecretExtraData()).isNull();
        Assertions.assertThat(upstreamInterface.getEndpoint()).isEqualTo("https://api.example.com/v1/messages");
        Assertions.assertThat(upstreamInterface.getExtraData()).isEqualTo("interfaceExtraData");
    }

    @Test
    void testRemoveSecrets_NullAndEmptyInputsDoNotThrow() {
        // given
        Upstream withNullInterfaces = new Upstream();
        withNullInterfaces.setKey("upstream-key");
        withNullInterfaces.setInterfaces(null);

        Map<String, UpstreamInterface> interfacesWithNullValue = new HashMap<>();
        interfacesWithNullValue.put("anthropicMessages", null);
        Upstream withNullInterfaceValue = new Upstream();
        withNullInterfaceValue.setSecretExtraData("upstreamSecretExtraData");
        withNullInterfaceValue.setInterfaces(interfacesWithNullValue);

        // when
        UpstreamSecretUtils.removeSecrets(null);
        UpstreamSecretUtils.removeSecrets(List.of());
        UpstreamSecretUtils.removeSecrets(List.of(withNullInterfaces, withNullInterfaceValue));

        // then
        Assertions.assertThat(withNullInterfaces.getKey()).isNull();
        Assertions.assertThat(withNullInterfaceValue.getSecretExtraData()).isNull();
    }

    @Test
    void testHasSecrets_UpstreamLevelSecret() {
        // given
        CoreUpstream withKey = new CoreUpstream();
        withKey.setKey("upstream-key");

        CoreUpstream withSecretExtraData = new CoreUpstream();
        withSecretExtraData.setSecretExtraData("upstreamSecretExtraData");

        // when
        // then
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(withKey)).isTrue();
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(withSecretExtraData)).isTrue();
    }

    @Test
    void testHasSecrets_InterfaceLevelSecretOnly() {
        // given
        CoreUpstreamInterface withKey = new CoreUpstreamInterface();
        withKey.setKey("interface-key");
        CoreUpstream upstreamWithInterfaceKey = new CoreUpstream();
        upstreamWithInterfaceKey.setEndpoint("https://api.example.com");
        upstreamWithInterfaceKey.setInterfaces(Map.of("anthropicMessages", withKey));

        CoreUpstreamInterface withSecretExtraData = new CoreUpstreamInterface();
        withSecretExtraData.setSecretExtraData("interfaceSecretExtraData");
        CoreUpstream upstreamWithInterfaceSecretExtraData = new CoreUpstream();
        upstreamWithInterfaceSecretExtraData.setEndpoint("https://api.example.com");
        upstreamWithInterfaceSecretExtraData.setInterfaces(Map.of("anthropicMessages", withSecretExtraData));

        // when
        // then
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(upstreamWithInterfaceKey)).isTrue();
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(upstreamWithInterfaceSecretExtraData)).isTrue();
    }

    @Test
    void testHasSecrets_SecretFreeUpstream() {
        // given
        CoreUpstreamInterface publicInterface = new CoreUpstreamInterface();
        publicInterface.setEndpoint("https://api.example.com/v1/messages");
        publicInterface.setExtraData("interfaceExtraData");

        CoreUpstream withPublicInterface = new CoreUpstream();
        withPublicInterface.setEndpoint("https://api.example.com");
        withPublicInterface.setExtraData("upstreamExtraData");
        withPublicInterface.setInterfaces(Map.of("anthropicMessages", publicInterface));

        CoreUpstream withNullInterfaces = new CoreUpstream();
        withNullInterfaces.setEndpoint("https://api.example.com");
        withNullInterfaces.setInterfaces(null);

        Map<String, CoreUpstreamInterface> interfacesWithNullValue = new HashMap<>();
        interfacesWithNullValue.put("anthropicMessages", null);
        CoreUpstream withNullInterfaceValue = new CoreUpstream();
        withNullInterfaceValue.setEndpoint("https://api.example.com");
        withNullInterfaceValue.setInterfaces(interfacesWithNullValue);

        // when
        // then
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(withPublicInterface)).isFalse();
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(withNullInterfaces)).isFalse();
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(withNullInterfaceValue)).isFalse();
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(new CoreUpstream())).isFalse();
        Assertions.assertThat(UpstreamSecretUtils.hasSecrets(null)).isFalse();
    }
}
