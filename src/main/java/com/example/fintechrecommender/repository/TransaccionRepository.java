package com.example.fintechrecommender.repository;

import com.example.fintechrecommender.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad legacy Transaccion.
 *
 * Solo expone los metodos heredados de JpaRepository. Se usa en el
 * flujo legacy de /api/transacciones.
 */
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {}
