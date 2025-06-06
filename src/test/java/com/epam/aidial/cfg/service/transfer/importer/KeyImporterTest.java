package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.core.config.CoreKey;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class KeyImporterTest {

    private KeyImporter keyImporter;

    @BeforeEach
    void init() {
        KeyService keyService = mock(KeyService.class);
        KeyCoreMapper mapper = Mappers.getMapper(KeyCoreMapper.class);
        keyImporter = new KeyImporter(keyService, mapper);
    }

    @ParameterizedTest
    @MethodSource("keys")
    void testMapKey(String key, CoreKey coreKey) {
        // given
        // when
        Key result = keyImporter.map(key, coreKey);
        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getKey()).isEqualTo(coreKey.getKey());
        Assertions.assertThat(result.getProject()).isEqualTo("testProject");
        Assertions.assertThat(result.getRoles()).containsExactlyInAnyOrder("default");

    }

    private static Stream<Arguments> keys() {
        CoreKey first = new CoreKey();
        first.setKey("testKey1");
        first.setProject("testProject");
        first.setRole("default");

        CoreKey second = new CoreKey();
        second.setKey("testKey2");
        second.setProject("testProject");
        second.setRoles(List.of("default"));
        return Stream.of(
                Arguments.of(first.getKey(), first),
                Arguments.of(second.getKey(), second)
        );
    }

}