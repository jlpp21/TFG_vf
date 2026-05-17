package com.example.fintechrecommender.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de healthcheck.
 *
 * Expone un GET /ping que devuelve un texto fijo. Sirve para que un sistema
 * externo (load balancer, monitor, etc.) pueda comprobar rapidamente que el
 * servidor esta vivo.
 */
@RestController
public class PingController {

    /**
     * Indica que el servidor esta activo.
     *
     * @return texto fijo "Servidor activo y funcionando".
     */
    @GetMapping("/ping")
    public String ping() {
        return "Servidor activo y funcionando";
    }
}

