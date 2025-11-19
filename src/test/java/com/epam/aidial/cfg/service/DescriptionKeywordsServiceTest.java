package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DescriptionKeywordsServiceTest {

    @Mock
    private ModelService modelService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationTypeSchemaService applicationTypeSchemaService;
    @Mock
    private ToolSetService toolSetService;

    @InjectMocks
    private DescriptionKeywordsService descriptionKeywordsService;

    @Test
    void testGetAllDescriptionKeywords() {

        when(modelService.getAll()).thenReturn(getModels());
        when(applicationService.getAllApplications()).thenReturn(getApplications());
        when(applicationTypeSchemaService.getAll()).thenReturn(getAppRunners());
        when(toolSetService.getAll()).thenReturn(getToolSets());

        Collection<String> allModels = descriptionKeywordsService.getAllDescriptionKeywords();

        assertThat(allModels).containsExactly(
                "Accounting",
                "Business Development",
                "Customer Service",
                "Engineering",
                "Finance",
                "MCP1",
                "MCP2",
                "Prod",
                "Test"
        );
    }

    private static List<ApplicationTypeSchema> getAppRunners() {
        ApplicationTypeSchema appRunner1 = new ApplicationTypeSchema();
        appRunner1.setTopics(Set.of(
                "Test"));
        ApplicationTypeSchema appRunner2 = new ApplicationTypeSchema();
        appRunner2.setTopics(Set.of(
                "Prod"));
        ApplicationTypeSchema appRunner3 = new ApplicationTypeSchema();
        appRunner3.setTopics(null);
        return List.of(appRunner1, appRunner2, appRunner3);
    }

    private static List<Application> getApplications() {
        Application application1 = new Application();
        LinkedHashSet<String> application1Topics = new LinkedHashSet<>(Set.of("Accounting"));
        application1.setDescriptionKeywords(application1Topics);

        Application application2 = new Application();
        LinkedHashSet<String> application2Topics = new LinkedHashSet<>(Set.of("Business Development"));
        application2.setDescriptionKeywords(application2Topics);

        Application application3 = new Application();
        application3.setDescriptionKeywords(null);

        return List.of(application1, application2, application3);
    }

    private static List<Model> getModels() {
        Model model1 = new Model();
        LinkedHashSet<String> model1Topics = new LinkedHashSet<>(Set.of("Customer Service", "Finance"));
        model1.setTopics(model1Topics);

        Model model2 = new Model();
        LinkedHashSet<String> model2Topics = new LinkedHashSet<>(Set.of("Accounting", "Engineering"));
        model2.setTopics(model2Topics);

        Model model3 = new Model();
        model3.setTopics(null);

        return List.of(model1, model2, model3);
    }

    private static List<ToolSet> getToolSets() {
        ToolSet toolset1 = new ToolSet();
        LinkedHashSet<String> toolset1Topics = new LinkedHashSet<>(Set.of("MCP1"));
        toolset1.setDescriptionKeywords(toolset1Topics);

        ToolSet toolset2 = new ToolSet();
        LinkedHashSet<String> toolset2Topics = new LinkedHashSet<>(Set.of("Engineering", "MCP2"));
        toolset2.setDescriptionKeywords(toolset2Topics);

        ToolSet toolset3 = new ToolSet();
        toolset3.setDescriptionKeywords(null);

        return List.of(toolset1, toolset2, toolset3);
    }

}