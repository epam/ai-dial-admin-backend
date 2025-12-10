package com.epam.aidial.cfg.security;

import com.epam.aidial.cfg.client.dto.TokenResponseDto;
import com.epam.aidial.cfg.utils.SecretUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AuthTokenTest {

    @Test
    void testToString() {
        var token = new AuthToken("abc123", 3600);
        try (MockedStatic<SecretUtils> mocked = mockStatic(SecretUtils.class)) {
            mocked.when(() -> SecretUtils.mask("abc123")).thenReturn("***MASKED***");
            String expected = "AuthToken(accessToken=***MASKED***, expiresIn=3600)";
            assertThat(token.toString()).isEqualTo(expected);
        }
    }

    @Test
    void testToUnsecureString() {
        var token = new AuthToken("abc123", 3600);
        var expected = "AuthToken(accessToken=abc123, expiresIn=3600)";
        assertThat(token.toUnsecureString()).isEqualTo(expected);
    }

    @Test
    void testFrom_withValidDto() {
        TokenResponseDto dto = mock(TokenResponseDto.class);
        when(dto.getAccessToken()).thenReturn("token456");
        when(dto.getExpiresIn()).thenReturn(1800);

        AuthToken token = AuthToken.from(dto);

        assertThat(token).isNotNull();
        assertThat(token.accessToken()).isEqualTo("token456");
        assertThat(token.expiresIn()).isEqualTo(1800);
    }

    @Test
    void testFrom_withNullDto() {
        assertThat(AuthToken.from(null)).isNull();
    }
}
