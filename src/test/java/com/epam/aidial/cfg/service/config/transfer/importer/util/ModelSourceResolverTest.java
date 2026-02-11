package com.epam.aidial.cfg.service.config.transfer.importer.util;

import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.ModelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelSourceResolverTest {

    @Mock
    private AdapterService adapterService;
    @Mock
    private ModelEndpointUtils modelEndpointUtils;
    @Mock
    private ModelSourceRetentionPolicy modelSourceRetentionPolicy;

    @InjectMocks
    private ModelSourceResolver resolver;

    @Test
    void resolveSourceForExistingModel_shouldReturnExistingSourceWhenRetentionPolicySaysRetain() {
        // given
        CoreModel coreModel = new CoreModel();

        ModelSource existingSource = new ModelEndpointsSource();

        Model model = new Model();
        model.setSource(existingSource);

        when(modelSourceRetentionPolicy.shouldRetainSource(coreModel, model)).thenReturn(true);

        // when
        ModelSource actual = resolver.resolveSourceForExistingModel(coreModel, model);

        // then
        assertThat(actual).isEqualTo(existingSource);
        verifyNoInteractions(modelEndpointUtils, adapterService);
    }

    @Test
    void resolveSourceForExistingModel_shouldResolveNewSourceWhenRetentionPolicySaysDoNotRetain() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://adapter/completions");
        coreModel.setType(ModelType.CHAT);

        ModelSource existingSource = new ModelEndpointsSource();
        Model model = new Model();
        model.setSource(existingSource);

        ModelEndpointComponents modelEndpointComponents = new ModelEndpointComponents(
                "https://adapter",
                "/completions"
        );

        Adapter adapter = new Adapter();
        adapter.setName("test-adapter");

        when(modelSourceRetentionPolicy.shouldRetainSource(coreModel, model))
                .thenReturn(false);
        when(modelEndpointUtils.parseModelEndpoint("https://adapter/completions", ModelType.CHAT))
                .thenReturn(modelEndpointComponents);
        when(adapterService.getByEndpoint("https://adapter"))
                .thenReturn(adapter);

        ModelAdapterSource expected = new ModelAdapterSource();
        expected.setAdapterName("test-adapter");
        expected.setCompletionEndpointPath("/completions");

        // when
        ModelSource actual = resolver.resolveSourceForExistingModel(coreModel, model);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveSourceForNewModel_shouldReturnModelEndpointsSourceWhenCoreModelEndpointIsNull() {
        // given
        CoreModel coreModel = new CoreModel();

        // when
        ModelSource actual = resolver.resolveSourceForNewModel(coreModel);

        // then
        assertThat(actual).isEqualTo(new ModelEndpointsSource());
        verifyNoInteractions(modelEndpointUtils, adapterService);
    }

    @Test
    void resolveSourceForNewModel_shouldReturnModelEndpointsSourceWhenParsingReturnsNull() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://endpoint");
        coreModel.setType(ModelType.CHAT);

        when(modelEndpointUtils.parseModelEndpoint("https://endpoint", ModelType.CHAT))
                .thenReturn(null);

        // when
        ModelSource actual = resolver.resolveSourceForNewModel(coreModel);

        // then
        assertThat(actual).isEqualTo(new ModelEndpointsSource());
        verifyNoInteractions(adapterService);
    }

    @Test
    void resolveSourceForNewModel_shouldReturnModelAdapterSourceWhenEndpointComponentsResolved() {
        // given
        CoreModel coreModel = new CoreModel();
        coreModel.setEndpoint("https://adapter/completions");
        coreModel.setType(ModelType.CHAT);

        ModelEndpointComponents modelEndpointComponents = new ModelEndpointComponents(
                "https://adapter",
                "/completions"
        );

        Adapter adapter = new Adapter();
        adapter.setName("test-adapter");

        when(modelEndpointUtils.parseModelEndpoint("https://adapter/completions", ModelType.CHAT))
                .thenReturn(modelEndpointComponents);
        when(adapterService.getByEndpoint("https://adapter"))
                .thenReturn(adapter);

        ModelAdapterSource expected = new ModelAdapterSource();
        expected.setAdapterName("test-adapter");
        expected.setCompletionEndpointPath("/completions");

        // when
        ModelSource actual = resolver.resolveSourceForNewModel(coreModel);

        // then
        assertThat(actual).isEqualTo(expected);
    }

}