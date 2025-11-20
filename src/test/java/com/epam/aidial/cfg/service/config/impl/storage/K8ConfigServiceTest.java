package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.cfg.configuration.K8sProperties;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class K8ConfigServiceTest {

    private static final String NAMESPACE = "namespace";

    private K8ConfigService k8ConfigService;

    @BeforeEach
    void setUp() {
        K8sProperties k8sProperties = new K8sProperties();
        k8sProperties.setNamespace(NAMESPACE);

        k8ConfigService = new K8ConfigService(k8sProperties, null);
    }

    @Test
    void readSecretEntry_shouldReturnDecodedSecretValue() {
        // given
        String secretName = "secretName";
        String secretKey = "secretKey";

        String expectedResult = "Kubernetes secret value";

        Secret secret = new Secret();
        secret.setData(Map.of(secretKey, "S3ViZXJuZXRlcyBzZWNyZXQgdmFsdWU="));

        KubernetesClient kubernetesClient = mock(KubernetesClient.class);
        MixedOperation<Secret, SecretList, Resource<Secret>> mixedOperation = mock(MixedOperation.class);
        NonNamespaceOperation<Secret, SecretList, Resource<Secret>> nonNamespaceOperation = mock(NonNamespaceOperation.class);
        Resource<Secret> resource = mock(Resource.class);

        when(kubernetesClient.secrets()).thenReturn(mixedOperation);
        when(mixedOperation.inNamespace(NAMESPACE)).thenReturn(nonNamespaceOperation);
        when(nonNamespaceOperation.withName(secretName)).thenReturn(resource);
        when(resource.get()).thenReturn(secret);

        // when
        Optional<String> actualResult = k8ConfigService.readSecretEntry(kubernetesClient, secretName, secretKey);

        // then
        Assertions.assertThat(actualResult).isPresent();
        Assertions.assertThat(actualResult.get()).isEqualTo(expectedResult);

    }
}