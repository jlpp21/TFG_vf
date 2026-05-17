package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.model.Cliente;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.repository.ClienteRepository;
import com.example.fintechrecommender.service.RecomendacionIaService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador REST de recomendaciones financieras, bajo /api/recomendaciones.
 *
 * Expone dos endpoints:
 * - POST /ia: genera recomendaciones reales con el motor IA (Ollama) usando
 *   los datos del usuario autenticado.
 * - GET /{clienteId}: endpoint legacy con recomendaciones hardcodeadas
 *   segun el nombre del cliente. Se mantiene por compatibilidad con la
 *   primera version del proyecto.
 */
@RestController
@RequestMapping("/api/recomendaciones")
@CrossOrigin(origins = "*")
public class RecomendacionController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RecomendacionIaService iaService;

    /**
     * Genera recomendaciones financieras para el usuario autenticado usando IA.
     *
     * @param usuario usuario autenticado.
     * @return 200 OK con un JSON {diagnostico, alertas, recomendaciones};
     *         503 Service Unavailable con {error} si Ollama falla o no responde.
     */
    @PostMapping("/ia")
    public ResponseEntity<?> generarConIa(@AuthenticationPrincipal Usuario usuario) {
        try {
            JsonNode resp = iaService.generar(usuario);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(503).body(err);
        }
    }

    /**
     * Endpoint legacy que devuelve recomendaciones fijas segun el nombre del cliente.
     *
     * @param clienteId id del cliente legacy.
     * @return lista de recomendaciones en texto plano. Si el cliente no existe,
     *         devuelve un texto fijo "Cliente no encontrado.".
     */
    @GetMapping("/{clienteId}")
    public List<String> getRecomendaciones(@PathVariable Long clienteId) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        List<String> recomendaciones = new ArrayList<>();

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            String nombre = cliente.getNombre().toLowerCase();
            if (nombre.contains("ana")) {
                recomendaciones.add("¡Ana, revisa tus suscripciones mensuales para optimizar tus gastos!");
                recomendaciones.add("Intenta ahorrar al menos un 10% de tus ingresos mensuales.");
            } else if (nombre.contains("carlos")) {
                recomendaciones.add("Carlos, evita compras impulsivas usando tu tarjeta de crédito.");
                recomendaciones.add("Considera invertir en un fondo de bajo riesgo.");
            } else {
                recomendaciones.add("Recuerda monitorear tus gastos recurrentes.");
                recomendaciones.add("Establece metas de ahorro mensuales.");
            }
        } else {
            recomendaciones.add("Cliente no encontrado.");
        }

        return recomendaciones;
    }
}
