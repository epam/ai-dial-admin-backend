package com.epam.aidial.cfg.client.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class CoreMetadataUtilsTest {

    @ParameterizedTest
    @MethodSource("testReplacePathSegmentParams")
    void testReplacePathSegment(String path, String oldPathSegment, String newPathSegment, String expectedResult) {
        String actualResult = CoreMetadataUtils.replacePathSegment(path, oldPathSegment, newPathSegment);
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> testReplacePathSegmentParams() {
        return Stream.of(
                Arguments.of(
                        "prompts/public/New%20folder%201/sf-test-public/Acuity/Injuries_Preexist__1.0.0",
                        "public/New folder 1/",
                        "public/New folder 2/",
                        "prompts/public/New%20folder%202/sf-test-public/Acuity/Injuries_Preexist__1.0.0"
                ),
                Arguments.of(
                        "prompts/public/New%20folder%201/public/New%20folder%201/Injuries_Preexist__1.0.0",
                        "public/New folder 1/",
                        "public/New folder 2/",
                        "prompts/public/New%20folder%202/public/New%20folder%201/Injuries_Preexist__1.0.0"
                ),
                Arguments.of(
                        "prompts/public/test/Injuries_Preexist__1.0.0",
                        "public/test/",
                        "public/test1/test2",
                        "prompts/public/test1/test2/Injuries_Preexist__1.0.0"
                ),
                Arguments.of(
                        "prompts/public/test1/test2/Injuries_Preexist__1.0.0",
                        "public/test1/test2",
                        "public/test/",
                        "prompts/public/test/Injuries_Preexist__1.0.0"
                )
        );
    }
}