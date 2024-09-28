package com.example.spring_keycloak.exception;

import org.springframework.security.core.AuthenticationException;

// classe, que é um objeto de exceção personalizado que desejo lançar sempre que algo errado ocorrer com um token de acesso.
// estende uma classe abstrata do Spring Security AuthenticationExceptionque é uma superclasse para todas as exceções relacionadas à autenticação.
public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
