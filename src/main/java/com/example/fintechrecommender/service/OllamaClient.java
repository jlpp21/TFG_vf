package com.example.fintechrecommender.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para Ollama (endpoint /api/chat).
 *
 * Envuelve la llamada al servidor local de Ollama, configurando timeouts
 * y forzando format=json para que el modelo siempre devuelva JSON
 * parseable. Lo usa RecomendacionIaService para pedir recomendaciones
 * financieras al modelo. Lee la URL base, el modelo y el timeout desde
 * application.properties (app.ollama.*).
 */
@Component
public class OllamaClient {

    private final RestTemplate http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final String defaultModel;

    /**
     * Construye el cliente con la configuracion de Ollama.
     *
     * @param builder        builder de RestTemplate para configurar timeouts.
     * @param baseUrl        URL base del servidor Ollama (ej: http://localhost:11434).
     * @param defaultModel   nombre del modelo a usar por defecto.
     * @param timeoutSeconds tiempo maximo de espera de respuesta del modelo en segundos.
     */
    public OllamaClient(RestTemplateBuilder builder,
                        @Value("${app.ollama.base-url}") String baseUrl,
                        @Value("${app.ollama.model}") String defaultModel,
                        @Value("${app.ollama.timeout-seconds}") int timeoutSeconds) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.defaultModel = defaultModel;
        this.http = builder
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Envia un prompt de chat (system + user) a Ollama y devuelve el contenido
     * como String. Forza format=json y usa una temperatura baja (0.1) para que
     * el resultado sea determinista y parseable.
     *
     * @param systemPrompt prompt del rol "system" (instrucciones al modelo).
     * @param userPrompt   prompt del rol "user" (datos del cliente).
     * @return texto del campo "message.content" devuelto por Ollama, que deberia ser un JSON.
     * @throws IllegalStateException si Ollama no responde, devuelve un formato inesperado o falla la red.
     */
    public String chatJson(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", defaultModel);
        body.put("stream", false);
        body.put("format", "json");

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", 0.1);
        options.put("top_p", 0.9);
        body.put("options", options);

        Map<String, String> sys = new LinkedHashMap<>();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        Map<String, String> usr = new LinkedHashMap<>();
        usr.put("role", "user");
        usr.put("content", userPrompt);
        body.put("messages", List.of(sys, usr));

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        try {
            String response = http.postForObject(baseUrl + "/api/chat",
                    new HttpEntity<>(body, h), String.class);
            JsonNode root = mapper.readTree(response);
            JsonNode message = root.path("message").path("content");
            if (message.isMissingNode() || message.isNull()) {
                throw new IllegalStateException("Respuesta de Ollama sin contenido: " + response);
            }
            return message.asText();
        } catch (Exception e) {
            throw new IllegalStateException("Error invocando a Ollama: " + e.getMessage(), e);
        }
    }
}
