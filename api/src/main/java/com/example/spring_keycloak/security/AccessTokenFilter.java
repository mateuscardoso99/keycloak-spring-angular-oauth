package com.example.spring_keycloak.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import com.example.spring_keycloak.exception.InvalidTokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * A ideia principal por trás do filtro no Spring Security é construir em cima da programação orientada a aspectos, 
 * no Spring é o Spring AOP. Em resumo, quando qualquer solicitação HTTP é recebida por um aplicativo, 
 * a primeira coisa que o Spring faz é passá-la primeiro para um ou muitos filtros (FilterChain). 
 * Esses filtros são componentes que podem validar a solicitação de entrada e bloqueá-la se algo não estiver certo, 
 * para que a solicitação nunca chegue a um controlador.
 * 
 * Esse filtro que verificará se na solicitação recebida há um cabeçalho de autorização e então validará se ele contém o token de acesso em um formato adequado.
 */

public class AccessTokenFilter extends AbstractAuthenticationProcessingFilter{
    private final JwtTokenValidator tokenVerifier; //classe personalizada que encapsula toda a lógica de validação do token de acesso

    public AccessTokenFilter(JwtTokenValidator jwtTokenValidator, AuthenticationManager authenticationManager, AuthenticationFailureHandler authenticationFailureHandler) {
        super(AnyRequestMatcher.INSTANCE);
        setAuthenticationManager(authenticationManager);
        setAuthenticationFailureHandler(authenticationFailureHandler);
        this.tokenVerifier = jwtTokenValidator;
    }


    /*
     * Os nomes dos dois métodos são autoexplicativos, mas resumidamente no primeiro colocaremos uma lógica para validar se em uma requisição de entrada há um token de acesso válido e no segundo haverá a lógica do que será feito quando a autenticação terminar com sucesso.
     */

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        //log.info("Attempting to authenticate for a request {}", request.getRequestURI());
        String authorizationHeader = extractAuthorizationHeaderAsString(request);
        AccessToken accessToken = tokenVerifier.validateAuthorizationHeader(authorizationHeader);
        JwtAuthentication auth = new JwtAuthentication(accessToken);
        auth.setAuthenticated(true);
        auth.setDetails(Map.of("username", accessToken.getUsername(), "email", accessToken.getEmail()));
        return this.getAuthenticationManager().authenticate(auth);
    }

    //após a autenticação bem-sucedida, um objeto Authentication (no nosso caso, a classe JwtAuthentication) é adicionado ao SecurityContextHolder, que armazena todas as informações sobre o usuário atual que está fazendo uma solicitação
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        //log.info("Successfully authentication for the request {}", request.getRequestURI());

        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    private String extractAuthorizationHeaderAsString(HttpServletRequest request) {
        try {
            return request.getHeader("Authorization");
        } catch (Exception ex){
            throw new InvalidTokenException("There is no Authorization header in a request", ex);
        }
    }
    
}
