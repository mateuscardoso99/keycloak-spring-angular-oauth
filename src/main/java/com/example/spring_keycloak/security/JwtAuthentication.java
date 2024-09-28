package com.example.spring_keycloak.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/*
 * classe wrapper que estende AbstractAuthenticationToken(que estende a interface Authentication) e mantém um objeto AccessToken
 * essa classe existe pq getAuthenticationManager().authenticate() recebe um objeto Authentication
 * só que Authentication é uma interface, então precisa de uma classe concreta que a implemente
 */
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final AccessToken accessToken;

    public JwtAuthentication(AccessToken accessToken) {
        super(accessToken.getAuthorities());
        this.accessToken = accessToken;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return accessToken.getValueAsString();
    }
}
