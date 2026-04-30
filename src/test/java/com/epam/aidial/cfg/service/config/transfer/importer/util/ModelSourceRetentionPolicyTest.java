package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.core.config.CoreModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelSourceRetentionPolicyTest {

    @Mock
    private AdapterService adapterService;

    @InjectMocks
    private ModelSourceRetentionPolicy policy;

    @Test
    void shouldRetainSource_returnsTrueWhenEndpointIsNull() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint(null);

        Model model = new Model();

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldRetainSource_returnsTrueWhenEndpointsAreEqual() {
        // given
        String endpoint = "https://endpoint";

        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint(endpoint);

        Model model = new Model();
        model.setEndpoint(endpoint);

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldRetainSource_returnsTrueWhenEndpointMatchesModelAdapterSource() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://adapter-base/completions");

        ModelAdapterSource adapterSource = new ModelAdapterSource();
        adapterSource.setAdapterName("test-adapter");
        adapterSource.setCompletionEndpointPath("/completions");

        Model model = new Model();
        model.setSource(adapterSource);

        Adapter adapter = new Adapter();
        adapter.setBaseEndpoint("https://adapter-base");

        when(adapterService.get("test-adapter")).thenReturn(adapter);

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldRetainSource_returnsFalseWhenEndpointsAreNotEqualAndNoAdapterMatch() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://core-endpoint");

        Model model = new Model();
        model.setEndpoint("https://model-endpoint");

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldChangeSource_returnsTrueWhenEndpointsAreNotEqualAndNoAdapterMatch() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://core-endpoint");

        Model model = new Model();
        model.setEndpoint("https://model-endpoint");

        // when
        boolean actual = policy.shouldChangeSource(coreModel, model);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldChangeSource_returnsFalseWhenEndpointsAreEqual() {
        // given
        String endpoint = "https://endpoint";

        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint(endpoint);

        Model model = new Model();
        model.setEndpoint(endpoint);

        // when
        boolean actual = policy.shouldChangeSource(coreModel, model);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldRetainSource_returnsTrueWhenResponsesEndpointMatchesModelAdapterSource() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setResponsesEndpoint("https://adapter/responses");

        ModelAdapterSource adapterSource = new ModelAdapterSource();
        adapterSource.setAdapterName("test-adapter");

        Model model = new Model();
        model.setSource(adapterSource);

        Adapter adapter = new Adapter();
        adapter.setResponsesEndpoint("https://adapter/responses");

        when(adapterService.get("test-adapter")).thenReturn(adapter);

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldRetainSource_returnsFalseWhenResponsesEndpointDoesNotMatchModelAdapterSource() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setResponsesEndpoint("https://new-adapter/responses");

        ModelAdapterSource adapterSource = new ModelAdapterSource();
        adapterSource.setAdapterName("test-adapter");

        Model model = new Model();
        model.setSource(adapterSource);

        Adapter adapter = new Adapter();
        adapter.setResponsesEndpoint("https://old-adapter/responses");

        when(adapterService.get("test-adapter")).thenReturn(adapter);

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldRetainSource_returnsTrueWhenBothEndpointAndResponsesEndpointMatchModelAdapterSource() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://adapter-base/completions");
        coreModel.setResponsesEndpoint("https://adapter/responses");

        ModelAdapterSource adapterSource = new ModelAdapterSource();
        adapterSource.setAdapterName("test-adapter");
        adapterSource.setCompletionEndpointPath("/completions");

        Model model = new Model();
        model.setSource(adapterSource);

        Adapter adapter = new Adapter();
        adapter.setBaseEndpoint("https://adapter-base");
        adapter.setResponsesEndpoint("https://adapter/responses");

        when(adapterService.get("test-adapter")).thenReturn(adapter);

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldRetainSource_returnsFalseWhenEndpointMatchesButResponsesEndpointDoesNot() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://adapter-base/completions");
        coreModel.setResponsesEndpoint("https://new-adapter/responses");

        ModelAdapterSource adapterSource = new ModelAdapterSource();
        adapterSource.setAdapterName("test-adapter");
        adapterSource.setCompletionEndpointPath("/completions");

        Model model = new Model();
        model.setSource(adapterSource);

        Adapter adapter = new Adapter();
        adapter.setBaseEndpoint("https://adapter-base");
        adapter.setResponsesEndpoint("https://old-adapter/responses");

        when(adapterService.get("test-adapter")).thenReturn(adapter);

        // when
        boolean actual = policy.shouldRetainSource(coreModel, model);

        // then
        assertThat(actual).isFalse();
    }

}