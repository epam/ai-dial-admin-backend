package com.epam.aidial.cfg.service.publication;

import com.epam.aidial.cfg.client.PublicationClient;
import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationInfosDto;
import com.epam.aidial.cfg.client.dto.PublicationPathDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.PublicationsPathDto;
import com.epam.aidial.cfg.client.dto.RejectPublicationsDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.client.dto.RuleFunctionDto;
import com.epam.aidial.cfg.client.dto.RulesDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PublicationInfo;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.RuleFunction;
import com.epam.aidial.cfg.service.publication.resolver.PublicationResolver;
import com.epam.aidial.cfg.service.publication.resolver.type.PublicationResourceTypeResolver;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.epam.aidial.cfg.client.dto.PublicationStatusDto.APPROVED;
import static com.epam.aidial.cfg.client.dto.PublicationStatusDto.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Mock
    private PublicationClient publicationClient;
    @Spy
    private PublicationClientMapperImpl publicationClientMapper;
    @Spy
    private PublicationResourceTypeResolver publicationResourceTypeResolver;
    @Mock
    private PublicationResolver promptPublicationResolver;

    private PublicationService publicationService;

    @BeforeEach
    void setUp() {
        var publicationResolversByResourceType = Map.of(ResourceType.PROMPT, promptPublicationResolver);
        publicationService = new PublicationService(publicationClient, publicationClientMapper, publicationResourceTypeResolver, publicationResolversByResourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "PROMPT, prompt_pending",
            "FILE, file_pending",
            "APPLICATION, application_pending",
            "CONVERSATION, conversation_pending"
    })
    void getAllPublications_ReturnsOnlyResourceTypedPublications(String resourceTypeAsString, String requestName) throws JsonProcessingException {
        // given
        ResourceType resourceType = ResourceType.valueOf(resourceTypeAsString);

        var dtoJson = ResourceUtils.readResource("/publications/client/publications.json");
        var dto = OBJECT_MAPPER.readValue(dtoJson, new TypeReference<PublicationInfosDto>() {
        });
        when(publicationClient.getPublications(any(PublicationsPathDto.class))).thenReturn(dto);

        // when
        var publications = publicationService.getAllPublications(resourceType);

        // then
        assertThat(publications).isNotNull();
        assertThat(publications.getPublications()).containsExactly(
                PublicationInfo.builder()
                        .path("bucket/" + requestName + "_id")
                        .requestName(requestName)
                        .author("John Dough")
                        .createdAt(12345)
                        .resourceTypes(List.of(resourceType))
                        .build()
        );
    }

    @Test
    void getAllPublications_ReturnsAllPublications() throws JsonProcessingException {
        // given
        var dtoJson = ResourceUtils.readResource("/publications/client/publications.json");
        var dto = OBJECT_MAPPER.readValue(dtoJson, new TypeReference<PublicationInfosDto>() {
        });
        when(publicationClient.getPublications(any(PublicationsPathDto.class))).thenReturn(dto);

        // when
        var publications = publicationService.getAllPublications(null);

        // then
        assertThat(publications).isNotNull();
        assertThat(publications.getPublications()).containsExactly(
                PublicationInfo.builder()
                        .path("bucket/prompt_pending_id")
                        .requestName("prompt_pending")
                        .author("John Dough")
                        .createdAt(12345)
                        .resourceTypes(List.of(ResourceType.PROMPT))
                        .build(),
                PublicationInfo.builder()
                        .path("bucket/file_pending_id")
                        .requestName("file_pending")
                        .author("John Dough")
                        .createdAt(12345)
                        .resourceTypes(List.of(ResourceType.FILE))
                        .build(),
                PublicationInfo.builder()
                        .path("bucket/application_pending_id")
                        .requestName("application_pending")
                        .author("John Dough")
                        .createdAt(12345)
                        .resourceTypes(List.of(ResourceType.APPLICATION))
                        .build(),
                PublicationInfo.builder()
                        .path("bucket/conversation_pending_id")
                        .requestName("conversation_pending")
                        .author("John Dough")
                        .createdAt(12345)
                        .resourceTypes(List.of(ResourceType.CONVERSATION))
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getPublication_InvalidStatus_ThrowsEntityNotFoundException_TestParams")
    void getPublication_InvalidStatus_ThrowsEntityNotFoundException(PublicationStatusDto publicationStatusDto) {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setStatus(publicationStatusDto);

        when(publicationClient.getPublication(any(PublicationPathDto.class))).thenReturn(publicationDto);

        // when
        var exception = assertThrows(EntityNotFoundException.class,
                () -> publicationService.getPublication(publicationPath));

        // then
        assertThat(exception).hasMessage("Publication not found: " + publicationPath);
    }

    @Test
    void getPublication_EmptyResourceTypes_ThrowsIllegalStateException() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setStatus(PublicationStatusDto.PENDING);

        when(publicationClient.getPublication(any(PublicationPathDto.class))).thenReturn(publicationDto);

        // when
        var exception = assertThrows(IllegalStateException.class,
                () -> publicationService.getPublication(publicationPath));

        // then
        assertThat(exception).hasMessage("Unable to resolve publication resource type. Resource types: []");
    }

    @Test
    void getPublication_UnknownResourceType_ThrowsIllegalStateException() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.FILE));

        when(publicationClient.getPublication(any(PublicationPathDto.class))).thenReturn(publicationDto);

        // when
        var exception = assertThrows(IllegalStateException.class,
                () -> publicationService.getPublication(publicationPath));

        // then
        assertThat(exception).hasMessage("Unable to find publication resolver. Resource type: FILE");
    }

    @Test
    void getPublication_ReturnsPublication() {
        // given
        var publicationPath = "testPublication";
        var fullPath = "publications/" + publicationPath;

        var publicationDto = new PublicationDto();
        publicationDto.setUrl(fullPath);
        publicationDto.setStatus(PublicationStatusDto.PENDING);
        publicationDto.setResourceTypes(List.of(ResourceTypeDto.PROMPT));

        var publication = new PromptPublication();

        when(publicationClient.getPublication(any(PublicationPathDto.class))).thenReturn(publicationDto);
        when(promptPublicationResolver.resolvePublication(publicationDto)).thenReturn(publication);

        // when
        var actualPublication = publicationService.getPublication(publicationPath);

        // then
        assertThat(actualPublication).isEqualTo(publication);
    }

    @Test
    void testCreatePublication() {
        // given
        PublicationResource resource = PublicationResource.builder()
                .action(PublicationResourceAction.DELETE)
                .targetUrl("files/public/test.json")
                .build();

        Rule rule = Rule.builder()
                .source("role")
                .function(RuleFunction.EQUAL)
                .targets(List.of("admin"))
                .build();

        CreatePublication createPublication = CreatePublication.builder()
                .targetFolder("public/")
                .resources(List.of(resource))
                .rules(List.of(rule))
                .build();

        PublicationDto publicationDto = PublicationDto.builder()
                .url("publications/publicationUrl")
                .build();

        when(publicationClient.createPublication(any())).thenReturn(publicationDto);

        // when
        String publication = publicationService.createPublication(createPublication);

        // then
        Assertions.assertThat(publication).isEqualTo("publications/publicationUrl");
    }

    @Test
    void testGetRules() {
        // given
        String path = "public/test/";
        RuleDto ruleDto = RuleDto.builder()
                .source("role")
                .function(RuleFunctionDto.EQUAL)
                .targets(List.of("admin"))
                .build();
        Map<String, List<RuleDto>> ruleDtos = Map.of(path, List.of(ruleDto));
        RulesDto rulesDto = RulesDto.builder()
                .rules(ruleDtos)
                .build();
        when(publicationClient.getRules(any())).thenReturn(rulesDto);
        // when
        Map<String, List<Rule>> result = publicationService.getRules(path);
        // then
        Assertions.assertThat(result).isNotEmpty().containsOnlyKeys(path).satisfies(rules ->
                Assertions.assertThat(rules.get(path)).hasSize(1).first().satisfies(rule -> {
                    Assertions.assertThat(rule.getSource()).isEqualTo("role");
                    Assertions.assertThat(rule.getFunction()).isEqualTo(RuleFunction.EQUAL);
                    Assertions.assertThat(rule.getTargets()).containsExactlyInAnyOrder("admin");
                })
        );
    }

    @ParameterizedTest
    @MethodSource("rejectPublication_TestParams")
    void testRejectPublication(String comment, String expected) {
        // given
        var publicationPath = "testPublication";

        // when
        publicationService.rejectPublication(publicationPath, comment);

        // then
        ArgumentCaptor<String> commentCaptor = ArgumentCaptor.forClass(String.class);
        verify(publicationClientMapper).toRejectPublicationDto(eq(publicationPath),
                commentCaptor.capture());

        String sanitizedComment = commentCaptor.getValue();
        assertEquals(sanitizedComment, expected);

        ArgumentCaptor<RejectPublicationsDto> dtoCaptor =
                ArgumentCaptor.forClass(RejectPublicationsDto.class);
        verify(publicationClient).rejectPublication(dtoCaptor.capture());

        assertEquals(dtoCaptor.getValue().getComment(), expected);
    }

    private static Stream<Arguments> getPublication_InvalidStatus_ThrowsEntityNotFoundException_TestParams() {
        return Stream.of(
                Arguments.of(APPROVED),
                Arguments.of(REJECTED)
        );
    }

    private static Stream<Arguments> rejectPublication_TestParams() {
        return Stream.of(
            Arguments.of("<script></script>Valid comment", "Valid comment"),
            Arguments.of("<script ></script>   Valid comment", "Valid comment"),
            Arguments.of("  Valid comment", "Valid comment"),
            Arguments.of("<br>Valid comment", "Valid comment"),
            Arguments.of("<HTML> Valid comment", "Valid comment"),
            Arguments.of("<HTML> </p>Valid comment", "Valid comment")
        );
    }

}