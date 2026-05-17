package com.example.fintechrecommender.repository;

import com.example.fintechrecommender.model.ObjetivoUsuario;
import com.example.fintechrecommender.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad ObjetivoUsuario.
 *
 * Permite listar los objetivos del usuario en orden cronologico y
 * borrarlos todos de golpe (cuando el usuario actualiza su lista de
 * objetivos por completo, primero borra y vuelve a guardar).
 */
public interface ObjetivoUsuarioRepository extends JpaRepository<ObjetivoUsuario, Long> {

    /**
     * Lista los objetivos del usuario en orden de creacion (mas antiguos primero).
     *
     * @param usuario usuario propietario de los objetivos.
     * @return lista de objetivos del usuario (vacia si no tiene).
     */
    List<ObjetivoUsuario> findByUsuarioOrderByFechaCreacionAsc(Usuario usuario);

    /**
     * Borra todos los objetivos asociados al usuario. Lanza una unica
     * sentencia DELETE en lugar de cargar las entidades una a una.
     *
     * @param usuario usuario cuyos objetivos se borran.
     */
    @Modifying
    @Query("DELETE FROM ObjetivoUsuario o WHERE o.usuario = :usuario")
    void borrarPorUsuario(@Param("usuario") Usuario usuario);
}
