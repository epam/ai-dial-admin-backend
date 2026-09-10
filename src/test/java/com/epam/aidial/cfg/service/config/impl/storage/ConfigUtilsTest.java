package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreUpstream;
import com.epam.aidial.core.config.CoreUpstreamInterface;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ConfigUtilsTest {

    @Test
    void testRemoveSecrets_DropsUpstreamWhoseOnlySecretIsInsideAnInterface() {
        // given
        Config config = new Config();
        config.getModels().put("model1", modelWith(interfaceOnlySecretUpstream(), publicUpstream()));
        config.getRoutes().put("route1", routeWith(interfaceOnlySecretUpstream(), publicUpstream()));

        CoreApplication application = new CoreApplication();
        LinkedHashMap<String, CoreRoute> applicationRoutes = new LinkedHashMap<>();
        applicationRoutes.put("appRoute1", routeWith(interfaceOnlySecretUpstream(), publicUpstream()));
        application.setRoutes(applicationRoutes);
        config.getApplications().put("app1", application);

        // when
        ConfigUtils.removeSecrets(config);

        // then
        assertOnlyPublicUpstreamLeft(config.getModels().get("model1").getUpstreams());
        assertOnlyPublicUpstreamLeft(config.getRoutes().get("route1").getUpstreams());
        assertOnlyPublicUpstreamLeft(config.getApplications().get("app1").getRoutes().get("appRoute1").getUpstreams());
    }

    @Test
    void testRemoveSecrets_KeepsFullyPublicUpstream() {
        // given
        Config config = new Config();
        config.getModels().put("model1", modelWith(publicUpstream()));

        // when
        ConfigUtils.removeSecrets(config);

        // then
        Assertions.assertThat(config.getModels().get("model1").getUpstreams()).hasSize(1)
                .satisfies(upstreams -> {
                    CoreUpstream upstream = upstreams.get(0);
                    Assertions.assertThat(upstream.getEndpoint()).isEqualTo("https://public.example.com");
                    Assertions.assertThat(upstream.getInterfaces().get("anthropicMessages").getExtraData())
                            .isEqualTo("interfaceExtraData");
                });
        Assertions.assertThat(config.getKeys()).isEmpty();
    }

    private void assertOnlyPublicUpstreamLeft(List<CoreUpstream> upstreams) {
        Assertions.assertThat(upstreams).hasSize(1)
                .allSatisfy(upstream ->
                        Assertions.assertThat(upstream.getEndpoint()).isEqualTo("https://public.example.com"));
    }

    private CoreModel modelWith(CoreUpstream... upstreams) {
        CoreModel model = new CoreModel();
        model.setUpstreams(List.of(upstreams));
        return model;
    }

    private CoreRoute routeWith(CoreUpstream... upstreams) {
        CoreRoute route = new CoreRoute();
        route.setUpstreams(List.of(upstreams));
        return route;
    }

    private CoreUpstream interfaceOnlySecretUpstream() {
        CoreUpstreamInterface secretInterface = new CoreUpstreamInterface();
        secretInterface.setEndpoint("https://secret.example.com/v1/messages");
        secretInterface.setKey("interface-key");
        secretInterface.setSecretExtraData("interfaceSecretExtraData");

        CoreUpstream upstream = new CoreUpstream();
        upstream.setEndpoint("https://secret.example.com");
        upstream.setInterfaces(Map.of("anthropicMessages", secretInterface));
        return upstream;
    }

    private CoreUpstream publicUpstream() {
        CoreUpstreamInterface publicInterface = new CoreUpstreamInterface();
        publicInterface.setEndpoint("https://public.example.com/v1/messages");
        publicInterface.setExtraData("interfaceExtraData");

        CoreUpstream upstream = new CoreUpstream();
        upstream.setEndpoint("https://public.example.com");
        upstream.setInterfaces(Map.of("anthropicMessages", publicInterface));
        return upstream;
    }
}
