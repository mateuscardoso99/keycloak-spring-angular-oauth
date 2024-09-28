package com.example.spring_keycloak.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_keycloak.model.Filme;

/*
 * objetivo é proteger ambos os endpoints para que somente usuários autorizados e aplicativos externos (clientes) 
 * possam usá-los. Para conseguir isso, forçaremos todos os consumidores da API a fornecer um token de acesso JWT válido 
 * em cada solicitação.
 * Para consumir cada um desses endpoints dentro do token de acesso, uma determinada função deve estar presente. 
 * Por exemplo, somente usuários com ADMINfunção devem ter permissão para obter informações do /moviesendpoint e 
 * somente usuários com VISITORfunção podem obter informações de um segundo endpoint.
 */
@RestController
public class FilmesController {
    Map<Long, Filme> filmes;

    public FilmesController() {
        filmes = Map.of(
                1L, new Filme("Star Wars: A New Hope", "George Lucas", 1977),
                2L, new Filme("Star Wars: The Empire Strikes Back", "George Lucas", 1980),
                3L, new Filme("Star Wars: Return of the Jedi", "George Lucas", 1983));
    }

    @GetMapping("/filmes")
    public List<Filme> getAllFilmes(){
        return new ArrayList<>(filmes.values());
    }

    @GetMapping("/filmes/{id}")
    public Filme getMovieById(@PathVariable("id") String id){
        return filmes.get(Long.valueOf(id));
    }
}
