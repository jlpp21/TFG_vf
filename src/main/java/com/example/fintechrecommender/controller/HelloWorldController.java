package com.example.fintechrecommender.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de ejemplo/saludo.
 *
 * Expone un unico endpoint GET /hello que devuelve un mensaje fijo.
 * Util para comprobar manualmente que la aplicacion esta arrancada.
 */
@RestController
public class HelloWorldController {

    /**
     * Devuelve un saludo de ejemplo.
     *
     * @return texto fijo "Hello World from Fintech Recommender!".
     */
    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello World from Fintech Recommender!";
    }
}
