package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ResourceCredentialClient;
import com.epam.aidial.cfg.client.mapper.ResourceCredentialClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ResourceSignInRequest;
import com.epam.aidial.cfg.model.ResourceSignOutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@LogExecution
public class ResourceCredentialService {
    private static final String SIGN_IN_MESSAGE = "The '%s' field must have a value. Authentication type: '%s'";

    private final ResourceCredentialClient resourceCredentialClient;
    private final ResourceCredentialClientMapper resourceCredentialMapper;

    public void signIn(ResourceSignInRequest request) {
        validateSignInRequest(request);
        resourceCredentialClient.signInToolSetResource(resourceCredentialMapper.toResourceSignInRequestDto(request));
    }

    public void signOut(ResourceSignOutRequest request) {
        resourceCredentialClient.signOutToolSetResource(resourceCredentialMapper.toResourceSignOutRequestDto(request));
    }

    private void validateSignInRequest(ResourceSignInRequest request) {
        var authenticationType = request.getAuthenticationType();
        switch (authenticationType) {
            case OAUTH -> {
                if (StringUtils.isBlank(request.getCode())) {
                    throw new IllegalArgumentException(String.format(SIGN_IN_MESSAGE, "code", authenticationType));
                }
            }
            case API_KEY -> {
                if (StringUtils.isBlank(request.getApiKey())) {
                    throw new IllegalArgumentException(String.format(SIGN_IN_MESSAGE, "apiKey", authenticationType));
                }
            }
            case NONE -> {
                if (StringUtils.isNotBlank(request.getApiKey()) || StringUtils.isNotBlank(request.getCode())) {
                    throw new IllegalArgumentException("Neither Api key nor Code is not required when auth type is None");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported authentication type: " + authenticationType);
        }
    }
}