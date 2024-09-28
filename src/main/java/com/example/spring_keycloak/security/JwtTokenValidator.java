package com.example.spring_keycloak.security;

import static java.util.Objects.isNull;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.spring_keycloak.exception.InvalidTokenException;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JwtTokenValidator {
    private final JwkProvider jwkProvider;

    public JwtTokenValidator(JwkProvider jwkProvider){
        this.jwkProvider = jwkProvider;
    }

    public AccessToken validateAuthorizationHeader(String authorizationHeader) throws InvalidTokenException {
        String tokenValue = subStringBearer(authorizationHeader);
        validateToken(tokenValue);
        return new AccessToken(tokenValue);
    }

    // extrai o token do cabeçalho Authorization
    private String subStringBearer(String authorizationHeader) {
        try {
            return authorizationHeader.substring(AccessToken.BEARER.length());
        } catch (Exception ex) {
            throw new InvalidTokenException("There is no AccessToken in a request header");
        }
    }

    private void validateToken(String value) {
        DecodedJWT decodedJWT = decodeToken(value);
        verifyTokenHeader(decodedJWT);
        verifySignature(decodedJWT);
        verifyPayload(decodedJWT);
    }

    //decodifica o token jwt
    private DecodedJWT decodeToken(String value) {
        if (isNull(value)){
            throw new InvalidTokenException("Token has not been provided");
        }

        DecodedJWT decodedJWT = JWT.decode(value);
        //log.debug("Token decoded successfully");
        return decodedJWT;
    }

    //verifica se o token é um token JWT
    private void verifyTokenHeader(DecodedJWT decodedJWT) {
        try {
            Preconditions.checkArgument(decodedJWT.getType().equals("JWT"));
            //log.debug("Token's header is correct");
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("Token is not JWT type", ex);
        }
    }




    /*
     * Seguindo em frente, temos agora uma parte muito importante 
     * — validar uma assinatura. 
     * Se uma assinatura não for válida, significará que alguém fez algo estranho com o token. 
     * Talvez um invasor tenha tentado modificar sua carga útil para chegar ao recurso desejado? 
     * Essa é uma das possibilidades das quais a assinatura está tentando proteger.
     * 
     * Em resumo, a assinatura é uma operação criptográfica na qual, como cabeçalho de entrada, carga útil e chave secreta são fornecidos e o resultado é uma assinatura. 
     * Se algo mudar em qualquer um desses três elementos, a assinatura será diferente. 
     * Existem várias maneiras de validar se a assinatura está correta e uma delas, que eu gostaria de usar, é a assinatura RS256 . 
     * Com essa abordagem, temos um par de chaves (segredos), a primeira é usada para criar uma assinatura e a segunda pode ser usada apenas para validar se uma assinatura está correta.
     * 
     * E é isso que gostaríamos de conseguir: 
     * para verificar se uma assinatura está correta, precisamos de alguma forma obter uma chave pública (JSON Web Key — JWK) 
     * e verificá-la com o que está dentro do token.
     * 
     * 
     * -------------------
     * 
     * JWKe jwkProvider: 
     * O primeiro é uma representação de uma chave pública 
     * e o segundo (que é uma implementação de uma JwkProviderinterface) é responsável por obtê-la. 
     * Ambas as classes são definidas em uma biblioteca com.auth0:jwks-rsa
     * 
     * Esta JwkProvider é apenas uma interface, portanto, precisamos escrever nossa própria implementação. 
     * No nosso caso, um provedor da chave pública é Keycloak (que também está gerando tokens), 
     * portanto, a classe relevante foi nomeada KeycloakJwkProvider.
     * 
     * 
     * Graças a clase KeycloakJwkProvider, temos uma representação JWK, portanto, agora podemos fazer uma validação de uma assinatura com uma chave pública. 
     * Para isso, usamos novamente classes auxiliares da lib com.auth0:java-jwt
     * primeiro para selecionar um algoritmo de assinatura e, em seguida, para fazer uma verificação.
     * 
     * Após validar que uma assinatura está correta, podemos ter certeza de que todas as informações dentro do payload estão seguras. 
     * Antes de prosseguir, precisamos também validar o payload, se o token não expirou, e se contém funções de usuário e tem informações de escopo.
     */
    private void verifySignature(DecodedJWT decodedJWT) {
        try {
            Jwk jwk = jwkProvider.get(decodedJWT.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(decodedJWT);
            //log.debug("Token's signature is correct");
        } catch (JwkException | SignatureVerificationException ex) {
            throw new InvalidTokenException("Token has invalid signature", ex);
        }
    }

    //valida o payload, se o token não expirou, e se contém funções de usuário e tem informações de escopo.
    private void verifyPayload(DecodedJWT decodedJWT) {
        JsonObject payloadAsJson = decodeTokenPayloadToJsonObject(decodedJWT); //transforma o payload do token em um json primeiro

        if (hasTokenExpired(payloadAsJson)) {
            throw new InvalidTokenException("Token has expired");
        }
        //log.debug("Token has not expired");

        if (!hasTokenRealmRolesClaim(payloadAsJson)) {
            throw new InvalidTokenException("Token doesn't contain claims with realm roles");
        }
        //log.debug("Token's payload contain claims with realm roles");

        if (!hasTokenScopeInfo(payloadAsJson)) {
            throw new InvalidTokenException("Token doesn't contain scope information");
        }
        //log.debug("Token's payload contain scope information");
    }

    //transforma o payload do token em um json
    private JsonObject decodeTokenPayloadToJsonObject(DecodedJWT decodedJWT) {
        try {
            String payloadAsString = decodedJWT.getPayload();
            return new Gson().fromJson(
                new String(Base64.getDecoder().decode(payloadAsString), StandardCharsets.UTF_8),
                JsonObject.class);
        } catch (RuntimeException exception){
            throw new InvalidTokenException("Invalid JWT or JSON format of each of the jwt parts", exception);
        }
    }


    //verifica se o token tá expirado
    private boolean hasTokenExpired(JsonObject payloadAsJson) {
        Instant expirationDatetime = extractExpirationDate(payloadAsJson);
        return Instant.now().isAfter(expirationDatetime);
    }

    private Instant extractExpirationDate(JsonObject payloadAsJson) {
        try {
            return Instant.ofEpochSecond(payloadAsJson.get("exp").getAsLong());
        } catch (NullPointerException ex) {
            throw new InvalidTokenException("There is no 'exp' claim in the token payload");
        }
    }

    //validar se dentro de um token há informações sobre funções de usuário que foram atribuídas no Keycloak
    //As funções no JWT geradas pelo Keycloak estão dentro do array "roles", que faz parte do campo "realm_access"
    private boolean hasTokenRealmRolesClaim(JsonObject payloadAsJson) {
        try {
            return payloadAsJson.getAsJsonObject("realm_access").getAsJsonArray("roles").size() > 0;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    //verifica se existe o campo "scope" que é o campo que informa o que se pode acessar do usuário
    private boolean hasTokenScopeInfo(JsonObject payloadAsJson) {
        return payloadAsJson.has("scope");
    }
}
