package com.example.demo;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.SecretBundle;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class DemoApplication {

    public static void main(String[] args) {

        class ClientSecretKeyVaultCredential extends KeyVaultCredentials {
            private String clientId;
            private String clientKey;

            public ClientSecretKeyVaultCredential(String clientId, String clientKey) {
                this.clientId = clientId;
                this.clientKey = clientKey;
            }

            @Override
            public String doAuthenticate(String authorization, String resource, String scope) {
                AuthenticationResult token = getAccessTokenFromClientCredentials(
                        authorization, resource, clientId, clientKey);
                return token.getAccessToken();
            }

            private AuthenticationResult getAccessTokenFromClientCredentials(
                    String authorization, String resource, String clientId, String clientKey) {
                AuthenticationContext context = null;
                AuthenticationResult result = null;
                ExecutorService service = null;
                try {
                    service = Executors.newFixedThreadPool(1);
                    context = new AuthenticationContext(authorization, false, service);
                    ClientCredential credentials = new ClientCredential(clientId, clientKey);
                    Future<AuthenticationResult> future = context.acquireToken(
                            resource, credentials, null);
                    result = future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    service.shutdown();
                }

                if (result == null) {
                    throw new RuntimeException("authentication result was null");
                }
                return result;
            }
        }
        KeyVaultClient client = new KeyVaultClient(new ClientSecretKeyVaultCredential("3cd2c22e-ebaa-4f56-b5ce-36c931267aff", "BtD7Q~Z5bshMULadwLBsfqVjznSMm6nT1WI3s"));
        SecretBundle secret = client.getSecret("https://demovaultspring.vault.azure.net/", "test1");
        System.out.println("Value is:" + secret.value());
    }
}
