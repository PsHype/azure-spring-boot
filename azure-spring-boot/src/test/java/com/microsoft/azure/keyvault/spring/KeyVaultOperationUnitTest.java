/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.spring;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.models.SecretItem;
import com.microsoft.rest.RestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyVaultOperationUnitTest {

    private static final String testPropertyName1 = "testPropertyName1";
    private static final String fakeVaultUri = "https://fake.vault.com";
    @Mock
    KeyVaultClient keyVaultClient;

    @Before
    public void setup() {

        PagedList<SecretItem> mockResult = new PagedList<SecretItem>() {
            @Override
            public Page<SecretItem> nextPage(String s) throws RestException, IOException {
                final MockPage page = new MockPage();
                return page;
            }
        };

        final SecretItem secretItem = new SecretItem();
        secretItem.withId(testPropertyName1);
        mockResult.add(secretItem);

        final SecretBundle secretBundle = new SecretBundle();
        secretBundle.withValue(testPropertyName1);
        secretBundle.withId(testPropertyName1);


        when(keyVaultClient.listSecrets(anyString())).thenReturn(mockResult);
        when(keyVaultClient.getSecret(anyString(), anyString())).thenReturn(secretBundle);

    }

    @Test
    public void testGet() {
        final KeyVaultOperation keyVaultOperation = createDefaultKeyVault(keyVaultClient);

        final String result = (String) keyVaultOperation.get(testPropertyName1);

        assertThat(result).isEqualTo(testPropertyName1);
    }

    @Test
    public void testList() {
        final KeyVaultOperation keyVaultOperation = createDefaultKeyVault(keyVaultClient);

        final String[] result = keyVaultOperation.list();

        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(testPropertyName1);
    }

    class MockPage implements Page<SecretItem> {

        final SecretItem mockSecretItem = new SecretItem();

        @Override
        public String nextPageLink() {
            return null;
        }

        @Override
        public List<SecretItem> items() {
            mockSecretItem.withId("testPropertyName1");
            return Arrays.asList(mockSecretItem);
        }
    }

    @Test
    public void should_never_fetch_listSecrets_when_policy_is_single() {
        final KeyVaultOperation keyVaultOperation = new KeyVaultOperation(keyVaultClient,
                fakeVaultUri,
                Constants.DEFAULT_REFRESH_INTERVAL_MS,
                VaultPolicy.SINGLE);

        final Object result = keyVaultOperation.get(testPropertyName1);

        assertThat(result).isEqualTo(testPropertyName1);
        Mockito.verify(keyVaultClient, never()).listSecrets(anyString());
    }

    @Test
    public void should_call_fetch_listSecrets_when_policy_is_single() {
        final KeyVaultOperation keyVaultOperation = createDefaultKeyVault(keyVaultClient);

        final Object result = keyVaultOperation.get(testPropertyName1);

        assertThat(result).isEqualTo(testPropertyName1);
        Mockito.verify(keyVaultClient).listSecrets(anyString());
    }

    private static KeyVaultOperation createDefaultKeyVault(final KeyVaultClient keyVaultClient) {
        return new KeyVaultOperation(keyVaultClient,
                KeyVaultOperationUnitTest.fakeVaultUri,
                Constants.DEFAULT_REFRESH_INTERVAL_MS,
                VaultPolicy.LIST);
    }
}
