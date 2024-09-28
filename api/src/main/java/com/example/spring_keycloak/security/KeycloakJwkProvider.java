package com.example.spring_keycloak.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.NetworkException;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;


/*
 * O JwkProvider define apenas um método public Jwk get(String keyId) 
 * que retorna representação JSON Web Key (JWK) por um dado kidque está disponível no cabeçalho do token de acesso. 
 * É um identificador de uma chave pública se um servidor de autorização suportar múltiplas chaves.
 * 
 * 
 * 
 * 
 * 
 * 
 * No construtor desta classe há jwkProviderUrl String que representa uma URL que precisa ser chamada por um aplicativo para buscar um JWK. 
 * E o que esta classe está realmente fazendo é usar esta URL pública para buscar JWK(s) e produzir uma Jwk representação Java dele. 
 * 
 * Um exemplo de tal URL para Keycloak seria: {keycloak_base_url}/auth/realms/{realm_name}/protocolo/openid-connect/certs
 * onde "keycloak_base_url" é a URL base do Keycloak (no meu caso localhost:8080) e realm_name é o nome de um realm (no meu caso test).
 * 
 * um exemplo da resposta seria:
 * {
        "keys": [{
            "kid": "WbggFCA72xPnJacTe4G37D7SCXZI9o0AVLMgtwK_jhQ",
            "kty": "RSA",
            "alg": "RS256",
            "use": "sig",
            "n": "k96-hliB-mYJx47V4wPqnWi89IsSYfmCXjiWgHG4e4VAEcLqZ33V9aQ3nConMf0AWbG9zcQZUtqSTtXVKeUvtx_7QH9Rgzwoj0XDOU3K21hcZCs7ShMK-2kROk2jnZpM5I6r7GeloR52fPXIck-sXcR3acmeUgZyR_y7TtlYYCn4GUt2kjrwyL_qdNdvQoDXvHTnxv4wk3_-Zff7I3xK3lnPE8uIz6gxjFMZ20-ruU_5-umpUvF9B6NUPf3LQTlMm864MBw1narN6QrWTp9c9DF0SeGcN8r_XMo0jDkMDjTWwrHiumnA8Il50hbmcp_WdMV8ivuVoONGLc_5Q6-uUQ",
            "e": "AQAB",
            "x5c": [
                "MIIClzCCAX8CBgF3GcqCYDANBgkqhkiG9w0BAQsFADAPMQ0wCwYDVQQDDAR0ZXN0MB4XDTIxMDExOTA4MzUzOFoXDTMxMDExOTA4MzcxOFowDzENMAsGA1UEAwwEdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJPevoZYgfpmCceO1eMD6p1ovPSLEmH5gl44loBxuHuFQBHC6md91fWkN5wqJzH9AFmxvc3EGVLakk7V1SnlL7cf+0B/UYM8KI9FwzlNyttYXGQrO0oTCvtpETpNo52aTOSOq+xnpaEednz1yHJPrF3Ed2nJnlIGckf8u07ZWGAp+BlLdpI68Mi/6nTXb0KA17x058b+MJN//mX3+yN8St5ZzxPLiM+oMYxTGdtPq7lP+frpqVLxfQejVD39y0E5TJvOuDAcNZ2qzekK1k6fXPQxdEnhnDfK/1zKNIw5DA401sKx4rppwPCJedIW5nKf1nTFfIr7laDjRi3P+UOvrlECAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAN+QZvSPXUP6Q9JxpYOzl6U7TXLAWmQPznf+gdSc4rmHQD5gKr2s8zeNuJrx+cIIxPGsMk04VGdNjKf8bRYvB7ta+POxQ5VRnHZS5XuEyGBFeSWbbzwZnQY+cWjY3AmyReenJ4QKal4iFZgNvkYbVaw9SX1qDXTXQTn6OXyz/Cp8SbMUW8fy7GqdGFLGKYhX1Irq4Reiidkw4pKVgGHblcjB2hIRfpC7TYnv0jL9RK8iymvPGYRQYka2ks8J/+HNudKO8MoOwghtP9jwTHEygioc9gAyf8DMS9cGnsFnisCz4B/etTyvyzuhyBgTili5WzXjOk1Y0NqaxY50618BvPg=="
            ],
            "x5t": "KQ4zgovMGPVesO36mzxvI9UTfdQ",
            "x5t#S256": "F4xuZVX7WsxcdXAetwwTOOI2ElV-hH9Gl_G25hANAaE"
        }]
    }
 */
public class KeycloakJwkProvider implements JwkProvider {

    private final URI uri;
    private final ObjectReader reader;

    public KeycloakJwkProvider(String jwkProviderUrl) {
        try {
            this.uri = new URI(jwkProviderUrl).normalize();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid jwks uri", e);
        }
        this.reader = new ObjectMapper().readerFor(Map.class);
    }

    @Override
    public Jwk get(String keyId) throws JwkException {
        final List<Jwk> jwks = getAll();
        if (keyId == null && jwks.size() == 1) {
            return jwks.get(0);
        }
        if (keyId != null) {
            for (Jwk jwk : jwks) {
                if (keyId.equals(jwk.getId())) {
                    return jwk;
                }
            }
        }
        throw new SigningKeyNotFoundException("No key found in " + uri.toString() + " with kid " + keyId, null);
    }

    private List<Jwk> getAll() throws SigningKeyNotFoundException {
        List<Jwk> jwks = new ArrayList<>();
        final List<Map<String, Object>> keys = (List<Map<String, Object>>) getJwks().get("keys");

        if (keys == null || keys.isEmpty()) {
            throw new SigningKeyNotFoundException("No keys found in " + uri.toString(), null);
        }

        try {
            for (Map<String, Object> values : keys) {
                jwks.add(Jwk.fromValues(values));
            }
        } catch (IllegalArgumentException e) {
            throw new SigningKeyNotFoundException("Failed to parse jwk from json", e);
        }
        return jwks;
    }

    //faz a requisição pro endpoint publico do keycloak {keycloak_base_url}/auth/realms/{realm_name}/protocolo/openid-connect/certs
    private Map<String, Object> getJwks() throws SigningKeyNotFoundException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(this.uri)
                    .headers("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return reader.readValue(response.body());

        } catch (IOException | InterruptedException e) {
            throw new NetworkException("Cannot obtain jwks from url " + uri.toString(), e);
        }
    }
}
