package com.example.spring_keycloak.security;

import static java.util.Objects.isNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.spring_keycloak.exception.InvalidTokenException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

//classe que representa um token JWT emitido pelo keycloak
public class AccessToken {
    public static final String BEARER = "Bearer ";

    private final String value;

    public AccessToken(String value){
        this.value = value;
    }

    public String getValueAsString() {
        return value;
    }

    public String getUsername(){
        return decodeToken(value).getClaim("name").toString();
    }

    public String getEmail(){
        return decodeToken(value).getClaim("email").toString();
    }

    //método auxiliar que retorna um Collection de GrantedAuthorities, no nosso caso, funções Keycloak.
    public Collection<? extends GrantedAuthority> getAuthorities() {
        DecodedJWT decodedJWT = decodeToken(value);
        JsonObject payloadAsJson = decodeTokenPayloadToJsonObject(decodedJWT);

       return StreamSupport.stream(
                payloadAsJson.getAsJsonObject("realm_access").getAsJsonArray("roles").spliterator(), false)
                .map(JsonElement::getAsString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private DecodedJWT decodeToken(String value) {
        if (isNull(value)){
            throw new InvalidTokenException("Token has not been provided");
        }
        return JWT.decode(value);
    }

    //transforma o payload do token em um json
    private JsonObject decodeTokenPayloadToJsonObject(DecodedJWT decodedJWT) {
        try {
            String payloadAsString = decodedJWT.getPayload();
            return new Gson().fromJson(
                    new String(Base64.getDecoder().decode(payloadAsString), StandardCharsets.UTF_8),
                    JsonObject.class);
        }   catch (RuntimeException exception){
            throw new InvalidTokenException("Invalid JWT or JSON format of each of the jwt parts", exception);
        }
    }
}
