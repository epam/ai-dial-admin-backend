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
        application1.setDescriptionKeywords(List.of(
                "Accounting"));
        Application application2 = new Application();
        application2.setDescriptionKeywords(List.of(
                "Business Development"));
        Application application3 = new Application();
        application3.setDescriptionKeywords(null);
        return List.of(application1, application2, application3);
    }

    private static List<Model> getModels() {
        Model model1 = new Model();
        model1.setTopics(List.of(
                "Customer Service",
                "Finance"));
        Model model2 = new Model();
        model2.setTopics(List.of(
                "Accounting",
                "Engineering"));
        Model model3 = new Model();
        model3.setTopics(null);
        return List.of(model1, model2, model3);
    }

    private static List<ToolSet> getToolSets() {
        ToolSet toolset1 = new ToolSet();
        toolset1.setDescriptionKeywords(List.of(
                "MCP1"));
        ToolSet toolset2 = new ToolSet();
        toolset2.setDescriptionKeywords(List.of(
                "Engineering",
                "MCP2"));
        ToolSet toolset3 = new ToolSet();
        toolset3.setDescriptionKeywords(null);
        return List.of(toolset1, toolset2, toolset3);
    }

}