package com.example.fintechrecommender.repository;

import com.example.fintechrecommender.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para la entidad legacy Cliente.
 *
 * Solo expone los metodos heredados de JpaRepository (save, findById,
 * findAll, deleteById, etc.). Se usa en el flujo legacy de /api/clientes.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {}
