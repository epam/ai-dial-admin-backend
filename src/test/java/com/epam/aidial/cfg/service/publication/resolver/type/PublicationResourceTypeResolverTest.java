package com.epam.aidial.cfg.service.publication.resolver.type;

import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.model.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationResourceTypeResolverTest {

    private PublicationResourceTypeResolver publicationResourceTypeResolver;

    @BeforeEach
    void setUp() {
        publicationResourceTypeResolver = new PublicationResourceTypeResolver();
    }

    @ParameterizedTest
    @MethodSource("resolveResourceTypeShouldReturnCorrectResourceTypeTestParams")
    void resolveResourceTypeShouldReturnCorrectResourceType(Collection<ResourceTypeDto> resourceTypes, ResourceType expectedResult) {
        ResourceType actualResult = publicationResourceTypeResolver.resolveResourceType(resourceTypes);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> resolveResourceTypeShouldReturnCorrectResourceTypeTestParams() {
        return Stream.of(
                Arguments.of(List.of(ResourceTypeDto.PROMPT), ResourceType.PROMPT),
                Arguments.of(List.of(ResourceTypeDto.TOOL_SET), ResourceType.TOOL_SET),
                Arguments.of(List.of(ResourceTypeDto.FILE), ResourceType.FILE),
                Arguments.of(List.of(ResourceTypeDto.APPLICATION), ResourceType.APPLICATION),
                Arguments.of(List.of(ResourceTypeDto.CONVERSATION), ResourceType.CONVERSATION),
                Arguments.of(List.of(ResourceTypeDto.APPLICATION, ResourceTypeDto.FILE), ResourceType.APPLICATION),
                Arguments.of(List.of(ResourceTypeDto.PROMPT, ResourceTypeDto.FILE), ResourceType.PROMPT),
                Arguments.of(List.of(ResourceTypeDto.CONVERSATION, ResourceTypeDto.FILE), ResourceType.CONVERSATION),
                Arguments.of(List.of(ResourceTypeDto.CONVERSATION, ResourceTypeDto.PROMPT), ResourceType.CONVERSATION)
        );
    }
}
