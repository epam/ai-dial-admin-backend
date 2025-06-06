package com.epam.aidial.cfg.service.publication.resolver.url;

import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.epam.aidial.cfg.client.dto.PublicationResourceActionDto.ADD;
import static com.epam.aidial.cfg.client.dto.PublicationResourceActionDto.DELETE;
import static com.epam.aidial.cfg.client.dto.PublicationStatusDto.APPROVED;
import static com.epam.aidial.cfg.client.dto.PublicationStatusDto.PENDING;
import static com.epam.aidial.cfg.client.dto.PublicationStatusDto.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;

class PublicationResourceUrlResolverTest {

    private static final String SOURCE_URL = "sourceUrl";
    private static final String TARGET_URL = "targetUrl";
    private static final String REVIEW_URL = "reviewUrl";

    private PublicationResourceUrlResolver publicationResourceUrlResolver;

    @BeforeEach
    void setUp() {
        publicationResourceUrlResolver = new PublicationResourceUrlResolver();
    }

    @ParameterizedTest
    @MethodSource("resolveUrlShouldReturnCorrectUrlTestParams")
    void resolveUrlShouldReturnCorrectUrl(PublicationResourceDto publicationResource, PublicationStatusDto status, String expectedResult) {
        String actualResult = publicationResourceUrlResolver.resolveUrl(publicationResource, status);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> resolveUrlShouldReturnCorrectUrlTestParams() {
        return Stream.of(
                Arguments.of(publicationResourceDto(ADD), PENDING, REVIEW_URL),
                Arguments.of(publicationResourceDto(ADD), APPROVED, TARGET_URL),
                Arguments.of(publicationResourceDto(ADD), REJECTED, SOURCE_URL),
                Arguments.of(publicationResourceDto(DELETE), PENDING, TARGET_URL),
                Arguments.of(publicationResourceDto(DELETE), APPROVED, TARGET_URL),
                Arguments.of(publicationResourceDto(DELETE), REJECTED, TARGET_URL)
        );
    }

    private static PublicationResourceDto publicationResourceDto(PublicationResourceActionDto actionDto) {
        return PublicationResourceDto.builder()
                .action(actionDto)
                .sourceUrl(SOURCE_URL)
                .targetUrl(TARGET_URL)
                .reviewUrl(REVIEW_URL)
                .build();
    }
}