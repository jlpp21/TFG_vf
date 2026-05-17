package com.example.fintechrecommender.repository;

import com.example.fintechrecommender.model.Movimiento;
import com.example.fintechrecommender.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Movimiento.
 *
 * Hereda de JpaRepository (save, findAll, etc.) y anyade una consulta
 * JPQL que recorre la relacion movimiento -> banco -> usuario para
 * traer todos los movimientos de un usuario en orden cronologico.
 */
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /**
     * Devuelve todos los movimientos de un usuario ordenados por fecha
     * ascendente y por id. Util para calcular saldos acumulados.
     *
     * @param usuario usuario del que se quieren los movimientos.
     * @return lista ordenada de movimientos (vacia si no tiene).
     */
    @Query("SELECT m FROM Movimiento m " +
           "WHERE m.banco.usuario = :usuario " +
           "ORDER BY m.fecha ASC, m.id ASC")
    List<Movimiento> findAllByUsuarioOrderedAsc(@Param("usuario") Usuario usuario);
}
