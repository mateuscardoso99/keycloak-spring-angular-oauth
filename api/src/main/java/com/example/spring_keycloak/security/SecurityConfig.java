package com.example.spring_keycloak.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.auth0.jwk.JwkProvider;
import com.example.spring_keycloak.exception.AccessTokenAuthenticationFailureHandler;

/*
 * @Order(1): um dos conceitos-chave do Spring Security são filtros que podem ser combinados em uma cadeia. Se precisarmos definir vários filtros, podemos especificar qual deles será usado em qual ordem. Ao adicionar esta anotação, dizemos que nosso filtro tem a maior prioridade (quanto menor o número, maior a prioridade),
 */

@Configuration
@EnableWebSecurity //habilita recursos de segurança
public class SecurityConfig {
    @Autowired
    private AuthenticationConfiguration authConfig;

    @Value("${spring.security.ignored}")
    private String urlsDesprotegidas;

    @Value("${keycloak.jwk-url}")
    private String jwkProviderUrl; //contém uma URL base do servidor de autorização, Keycloak no nosso caso, que fornece uma chave pública necessária para validar a assinatura de um token de acesso. O valor contido nela é usado para criar um Spring bean em JwkProvider keycloakJwkProvider()

    @Bean
    public AuthenticationProvider getAuthenticationProvider() {
        return new KeycloakAuthenticationProvider();
    }

    @Bean
    public AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return new AccessTokenAuthenticationFailureHandler();
    }

    @Bean
    public JwtTokenValidator jwtTokenValidator(JwkProvider jwkProvider) {
        return new JwtTokenValidator(jwkProvider);
    }

    @Bean
    public JwkProvider keycloakJwkProvider() {
        return new KeycloakJwkProvider(jwkProviderUrl);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(true);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.cors(v -> v.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//significa que as informações do usuário não são armazenadas na memória entre as solicitações e impede que o Spring crie sessões HTTP
            .authorizeHttpRequests((request) -> {
                request.requestMatchers(HttpMethod.GET,"/publico/**").permitAll()
                    //.requestMatchers(HttpMethod.GET,"/filmes/**").hasRole("VISITANTE")
                    .anyRequest().authenticated();
                }
            )
            .addFilterBefore(
                new AccessTokenFilter(
                    jwtTokenValidator(keycloakJwkProvider()),
                    authConfig.getAuthenticationManager(),
                    getAuthenticationFailureHandler()
                ),
                BasicAuthenticationFilter.class
            )
            .authenticationProvider(getAuthenticationProvider());
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:4200");//adiciona as origens permitidas
        configuration.addAllowedMethod(CorsConfiguration.ALL);
        configuration.setAllowedHeaders(List.of(
                "X-Requested-With", "Content-Type",
                "Authorization", "Origin", "Accept",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
