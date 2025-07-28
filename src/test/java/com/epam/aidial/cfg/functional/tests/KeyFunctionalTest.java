package com.epam.aidial.cfg.functional.tests;

import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import com.epam.aidial.cfg.web.facade.KeyFacade;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public abstract class KeyFunctionalTest {

    @Autowired
    private KeyFacade keyFacade;
    @Autowired
    private RoleFacade roleFacade;
    @Autowired
    private TransactionTimestampContext transactionTimestampContext;

    @BeforeEach
    public void beforeEach() {
        initRoles();
    }

    private void initRoles() {
        RoleDto role1 = roleDto("1");
        RoleDto role2 = roleDto("2");
        RoleDto role3 = roleDto("3");
        roleFacade.createRole(role1);
        roleFacade.createRole(role2);
        roleFacade.createRole(role3);
    }

    private RoleDto roleDto(String suffix) {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("role" + suffix);
        roleDto.setDescription("role" + suffix);
        return roleDto;
    }

    @Test
    public void shouldSuccessfullyCreateAndGetKeys() {
        doReturn(1L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);

        KeyDto actual = keyFacade.getKey(keyDto.getName());

        keyDto.setCreatedAt(Instant.ofEpochMilli(1L));
        keyDto.setKeyGeneratedAt(Instant.ofEpochMilli(1L));
        assertKey(actual, keyDto);

        doReturn(2L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto2 = createDto("2");
        keyFacade.createKey(keyDto2);

        Collection<KeyDto> actualKeys = keyFacade.getAllKeys();

        keyDto2.setCreatedAt(Instant.ofEpochMilli(2L));
        keyDto2.setKeyGeneratedAt(Instant.ofEpochMilli(2L));
        assertKeys(actualKeys, List.of(keyDto, keyDto2), false);
    }

    @Test
    public void shouldSuccessfullyCreateWithEmptyRolesAndGetKeys() {
        KeyDto keyDto = createDtoWithoutRoles("1");
        keyFacade.createKey(keyDto);

        KeyDto actual = keyFacade.getKey(keyDto.getName());
        keyDto.setRoles(List.of());
        assertKeyExcludingGeneratedFields(actual, keyDto);

        KeyDto keyDto2 = createDtoWithoutRoles("2");
        keyFacade.createKey(keyDto2);

        Collection<KeyDto> actualKeys = keyFacade.getAllKeys();
        Collection<KeyDto> expectedDtos = List.of(keyDto, keyDto2);
        expectedDtos.forEach(dto -> dto.setRoles(List.of()));
        assertKeys(actualKeys, expectedDtos, true);
    }

    @Test
    public void shouldSuccessfullyCreateAndDeleteKey() {
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);

        keyFacade.deleteKey(keyDto.getName());

        Assertions.assertThrows(EntityNotFoundException.class, () -> keyFacade.getKey(keyDto.getName()));
        Assertions.assertTrue(keyFacade.getAllKeys().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreateAndUpdateKey() {
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);
        KeyDto updatedKey = createDto("1");
        updatedKey.setDescription("new key description");

        keyFacade.updateKey(keyDto.getName(), updatedKey);

        KeyDto actual = keyFacade.getKey(keyDto.getName());
        var expected = createDto("1");
        expected.setDescription("new key description");
        assertKeyExcludingGeneratedFields(actual, expected);
    }

    @Test
    public void shouldThrowExceptionWhenRenameKey() {
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);
        KeyDto updatedKey = createDto("2");
        updatedKey.setDescription("new key description");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> keyFacade.updateKey(keyDto.getName(), updatedKey)
        );
        Assertions.assertEquals("Key with name: 'key1' can not be renamed. New key name: 'key2'", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenCreateKeyWithExistingValue() {
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);
        KeyDto keyDto2 = createDto("2");
        keyDto2.setKey("keyValue1");

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> keyFacade.createKey(keyDto2)
        );
        Assertions.assertEquals("Key with value keyValue1 already exists", exception.getMessage());
    }

    @Test
    public void shouldUpdateKeyGeneratedAtFieldWhenUpdateKeyValue() {
        doReturn(1L).when(transactionTimestampContext).getTimestamp();
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);

        doReturn(2L).when(transactionTimestampContext).getTimestamp();
        keyDto.setKey("new keyValue");
        keyFacade.updateKey(keyDto.getName(), keyDto);

        KeyDto actual = keyFacade.getKey(keyDto.getName());

        var expected = createDto("1");
        expected.setKey("new keyValue");
        expected.setCreatedAt(Instant.ofEpochMilli((1L)));
        expected.setKeyGeneratedAt(Instant.ofEpochMilli((2L)));

        assertKey(actual, expected);
    }

    @Test
    public void shouldSuccessfullyUpdateRoles() {
        // create key1: roles=[role1, role2]; key2: roles=[role1, role3]
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);
        KeyDto keyDto2 = createDto("2", List.of("role1", "role3"));
        keyFacade.createKey(keyDto2);

        // check role1: keys=[key1, key2]; role2: keys=[key1]; role3: keys=[key2]
        RoleDto role1 = roleFacade.getRole("role1");
        Assertions.assertEquals(List.of("key1", "key2"), role1.getGrantedKeys());
        RoleDto role2 = roleFacade.getRole("role2");
        Assertions.assertEquals(List.of("key1"), role2.getGrantedKeys());
        RoleDto role3 = roleFacade.getRole("role3");
        Assertions.assertEquals(List.of("key2"), role3.getGrantedKeys());

        // update key1: roles=[role2, role3]
        KeyDto updatedKeyDto = createDto("1", List.of("role2", "role3"));
        keyFacade.updateKey(updatedKeyDto.getName(), updatedKeyDto);

        // check key1: roles=[role2, role3]
        KeyDto actual = keyFacade.getKey(updatedKeyDto.getName());
        assertKeyExcludingGeneratedFields(actual, updatedKeyDto);

        // check role1: keys=[key2]; role2: keys=[key1]; role3: keys=[key2, key1]
        role1 = roleFacade.getRole("role1");
        Assertions.assertEquals(List.of("key2"), role1.getGrantedKeys());
        role2 = roleFacade.getRole("role2");
        Assertions.assertEquals(List.of("key1"), role2.getGrantedKeys());
        role3 = roleFacade.getRole("role3");
        org.assertj.core.api.Assertions.assertThat(role3.getGrantedKeys())
                .containsExactlyInAnyOrderElementsOf(List.of("key1", "key2"));
    }

    @Test
    public void shouldThrowExceptionWhenCreatingWithNonExistentRoles() {
        KeyDto keyDto = createDto("1", List.of("role1", "role4", "role5"));
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> keyFacade.createKey(keyDto)
        );

        Assertions.assertEquals("unable to find roles: [role4, role5]", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingWithNonExistentRoles() {
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);

        KeyDto updatedKeyDto = createDto("1", List.of("role1", "role4", "role5"));
        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> keyFacade.updateKey(updatedKeyDto.getName(), updatedKeyDto)
        );

        Assertions.assertEquals("unable to find roles: [role4, role5]", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyUpdateKeysWhenDeleteRole() {
        // create key1: roles=[role1, role2]; key2: roles=[role1, role3]
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);
        KeyDto keyDto2 = createDto("2", List.of("role1", "role3"));
        keyFacade.createKey(keyDto2);

        // check role1: keys=[key1, key2]; role2: keys=[key1]; role3: keys=[key2]
        RoleDto role1 = roleFacade.getRole("role1");
        Assertions.assertEquals(List.of("key1", "key2"), role1.getGrantedKeys());
        RoleDto role2 = roleFacade.getRole("role2");
        Assertions.assertEquals(List.of("key1"), role2.getGrantedKeys());
        RoleDto role3 = roleFacade.getRole("role3");
        Assertions.assertEquals(List.of("key2"), role3.getGrantedKeys());

        // delete role1
        roleFacade.deleteRole("role1");

        // check key1: roles=[role2]; key2: roles=[role3]
        KeyDto expectedKeyDto1 = createDto("1", List.of("role2"));
        KeyDto expectedKeyDto2 = createDto("2", List.of("role3"));
        KeyDto actualKey1 = keyFacade.getKey("key1");
        KeyDto actualKey2 = keyFacade.getKey("key2");
        assertKeyExcludingGeneratedFields(actualKey1, expectedKeyDto1);
        assertKeyExcludingGeneratedFields(actualKey2, expectedKeyDto2);
    }

    @Test
    public void shouldSuccessfullyUpdateRolesWhenDeleteKey() {
        // create key1: roles=[role1, role2]; key2: roles=[role1, role3]
        KeyDto keyDto = createDto("1", List.of("role1", "role2"));
        keyFacade.createKey(keyDto);
        KeyDto keyDto2 = createDto("2", List.of("role1", "role3"));
        keyFacade.createKey(keyDto2);

        // check role1: keys=[key1, key2]; role2: keys=[key1]; role3: keys=[key2]
        RoleDto role1 = roleFacade.getRole("role1");
        Assertions.assertEquals(List.of("key1", "key2"), role1.getGrantedKeys());
        RoleDto role2 = roleFacade.getRole("role2");
        Assertions.assertEquals(List.of("key1"), role2.getGrantedKeys());
        RoleDto role3 = roleFacade.getRole("role3");
        Assertions.assertEquals(List.of("key2"), role3.getGrantedKeys());

        // delete key1
        keyFacade.deleteKey("key1");

        // check role1: keys=[key1]; role2: keys=[]; role3: keys=[key2]
        role1 = roleFacade.getRole("role1");
        Assertions.assertEquals(List.of("key2"), role1.getGrantedKeys());
        role2 = roleFacade.getRole("role2");
        Assertions.assertEquals(List.of(), role2.getGrantedKeys());
        role3 = roleFacade.getRole("role3");
        Assertions.assertEquals(List.of("key2"), role3.getGrantedKeys());
    }

    @Test
    public void shouldThrowExceptionWhenCreateKeyWithExistingKeyName() {
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);

        EntityAlreadyExistsException exception = Assertions.assertThrows(
                EntityAlreadyExistsException.class,
                () -> keyFacade.createKey(createDto("1"))
        );

        Assertions.assertEquals("Key with name key1 already exists", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenUpdateKeyWithExistingKeyName() {
        KeyDto keyDto = createDto("1");
        keyFacade.createKey(keyDto);

        KeyDto keyDto2 = createDto("2");
        keyFacade.createKey(keyDto2);

        keyDto.setName("key2");

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> keyFacade.updateKey("key1", keyDto)
        );

        Assertions.assertEquals("Key with name: 'key1' can not be renamed. New key name: 'key2'", exception.getMessage());
    }

    private void assertKeys(Collection<KeyDto> actual, Collection<KeyDto> expected, boolean excludeGeneratedFields) {
        expected.forEach(keyDto -> keyDto.setKey(null));
        Map<String, KeyDto> actualMap = toMap(actual);
        Map<String, KeyDto> expectedMap = toMap(expected);
        Assertions.assertEquals(expectedMap.keySet(), actualMap.keySet());
        for (String name : actualMap.keySet()) {
            if (excludeGeneratedFields) {
                assertKeyExcludingGeneratedFields(actualMap.get(name), expectedMap.get(name));
            } else {
                assertKey(actualMap.get(name), expectedMap.get(name));
            }
        }
    }

    private Map<String, KeyDto> toMap(Collection<KeyDto> dtos) {
        return dtos.stream()
                .collect(Collectors.toMap(KeyDto::getName, Function.identity()));
    }

    private void assertKeyExcludingGeneratedFields(KeyDto actual, KeyDto expected) {
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt", "keyGeneratedAt")
                .isEqualTo(expected);
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getKeyGeneratedAt()).isNotNull();
    }

    private void assertKey(KeyDto actual, KeyDto expected) {
        assertThat(actual).isEqualTo(expected);
    }

    private KeyDto createDtoWithoutRoles(String suffix) {
        return createDto(suffix, null);
    }

    private KeyDto createDto(String suffix) {
        return createDto(suffix, List.of("role" + suffix));
    }

    private KeyDto createDto(String suffix, List<String> roles) {
        KeyDto keyDto = new KeyDto();
        keyDto.setName("key" + suffix);
        keyDto.setKey("keyValue" + suffix);
        keyDto.setDescription("description" + suffix);
        keyDto.setRoles(roles);
        keyDto.setProjectContactPoint("test@mail.com");
        keyDto.setExpiresAt(Instant.ofEpochMilli(253402300799999L));
        return keyDto;
    }
}
