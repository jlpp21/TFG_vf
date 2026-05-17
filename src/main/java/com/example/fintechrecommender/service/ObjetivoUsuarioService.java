package com.example.fintechrecommender.service;

import com.example.fintechrecommender.dto.ObjetivoUsuarioDto;
import com.example.fintechrecommender.model.ObjetivoUsuario;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.repository.ObjetivoUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona los objetivos financieros del usuario.
 *
 * Permite listar los objetivos actuales y reemplazarlos en bloque
 * (borra los antiguos y guarda los nuevos en una sola transaccion).
 * Valida que el codigo del objetivo y el plazo esten dentro de los
 * valores aceptados.
 */
@Service
public class ObjetivoUsuarioService {

    /** Conjunto de codigos de objetivo aceptados por el sistema. */
    private static final Set<String> OBJETIVOS_VALIDOS = new HashSet<>(Arrays.asList(
            "comprar_piso",
            "inversion_inmobiliaria",
            "inversion_cripto",
            "inversion_empresas",
            "inversion_bolsa",
            "plan_jubilacion",
            "fondo_emergencia",
            "ahorrar_capital",
            "dedicarse_inversion",
            "estudios"
    ));

    /** Conjunto de plazos aceptados. */
    private static final Set<String> PLAZOS_VALIDOS = new HashSet<>(Arrays.asList("CORTO", "MEDIO", "LARGO"));

    private final ObjetivoUsuarioRepository repo;

    /**
     * Construye el servicio con su repositorio.
     *
     * @param repo repositorio de ObjetivoUsuario.
     */
    public ObjetivoUsuarioService(ObjetivoUsuarioRepository repo) {
        this.repo = repo;
    }

    /**
     * Lista los objetivos del usuario en orden de creacion ascendente.
     *
     * @param usuario usuario propietario de los objetivos.
     * @return lista de ObjetivoUsuarioDto (vacia si no tiene ninguno).
     */
    @Transactional(readOnly = true)
    public List<ObjetivoUsuarioDto> listar(Usuario usuario) {
        return repo.findByUsuarioOrderByFechaCreacionAsc(usuario).stream()
                .map(o -> new ObjetivoUsuarioDto(o.getObjetivo(), o.getPlazo()))
                .collect(Collectors.toList());
    }

    /**
     * Reemplaza la lista completa de objetivos del usuario por una nueva.
     * Primero valida todas las entradas, luego borra los anteriores y
     * finalmente persiste los nuevos eliminando duplicados de codigo.
     *
     * @param usuario  usuario al que se asignan los objetivos.
     * @param entradas lista nueva de objetivos (puede ser null o vacia para borrar todo).
     * @return la lista persistida tras el reemplazo.
     * @throws IllegalArgumentException si algun objetivo o plazo no es valido.
     */
    @Transactional
    public List<ObjetivoUsuarioDto> reemplazar(Usuario usuario, List<ObjetivoUsuarioDto> entradas) {
        if (entradas == null) entradas = java.util.Collections.emptyList();
        for (ObjetivoUsuarioDto e : entradas) {
            if (e.getObjetivo() == null || !OBJETIVOS_VALIDOS.contains(e.getObjetivo())) {
                throw new IllegalArgumentException("Objetivo no valido: " + e.getObjetivo());
            }
            if (e.getPlazo() == null || !PLAZOS_VALIDOS.contains(e.getPlazo().toUpperCase())) {
                throw new IllegalArgumentException("Plazo no valido para " + e.getObjetivo());
            }
        }

        repo.borrarPorUsuario(usuario);
        repo.flush();

        Set<String> vistos = new HashSet<>();
        for (ObjetivoUsuarioDto e : entradas) {
            if (!vistos.add(e.getObjetivo())) continue;
            ObjetivoUsuario obj = new ObjetivoUsuario();
            obj.setUsuario(usuario);
            obj.setObjetivo(e.getObjetivo());
            obj.setPlazo(e.getPlazo().toUpperCase());
            repo.save(obj);
        }
        return listar(usuario);
    }
}
