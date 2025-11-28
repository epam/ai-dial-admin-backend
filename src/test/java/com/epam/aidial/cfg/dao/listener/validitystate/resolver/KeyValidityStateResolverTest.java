package com.epam.aidial.cfg.dao.listener.validitystate.resolver;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class KeyValidityStateResolverTest {

    KeyValidityStateResolver keyValidityStateResolver;

    @BeforeEach
    void setUp() {
        keyValidityStateResolver = new KeyValidityStateResolver();
    }

    @Test
    void resolveValidityState_shouldReturnValidStateWhenKeyHasRolesAndKeyValue() {
        // given
        RoleEntity roleEntity = new RoleEntity();

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setRoles(List.of(roleEntity));
        keyEntity.setKey("key");

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setValid(true);

        // when
        ValidityStateEntity actual = keyValidityStateResolver.resolveValidityState(keyEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnInvalidStateWhenKeyDoesNotHaveRoles() {
        // given
        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setKey("key");

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setMessage("No roles assigned");
        expected.setValid(false);

        // when
        ValidityStateEntity actual = keyValidityStateResolver.resolveValidityState(keyEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnInvalidStateWhenKeyDoesNotHaveKeyValue() {
        // given
        RoleEntity roleEntity = new RoleEntity();

        KeyEntity keyEntity = new KeyEntity();
        keyEntity.setRoles(List.of(roleEntity));

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setMessage("Key value is missing");
        expected.setValid(false);

        // when
        ValidityStateEntity actual = keyValidityStateResolver.resolveValidityState(keyEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void resolveValidityState_shouldReturnInvalidStateWhenKeyDoesNotHaveRolesAndKeyValue() {
        // given
        KeyEntity keyEntity = new KeyEntity();

        ValidityStateEntity expected = new ValidityStateEntity();
        expected.setMessage("No roles assigned, Key value is missing");
        expected.setValid(false);

        // when
        ValidityStateEntity actual = keyValidityStateResolver.resolveValidityState(keyEntity);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}