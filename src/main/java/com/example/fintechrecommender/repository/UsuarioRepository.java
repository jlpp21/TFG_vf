package com.example.fintechrecommender.repository;

import com.example.fintechrecommender.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Usuario.
 *
 * Permite buscar usuarios por correo (login) y comprobar si un correo
 * ya esta registrado antes de crear una nueva cuenta.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electronico.
     *
     * @param correo correo del usuario a buscar.
     * @return Optional con el usuario si existe, vacio en caso contrario.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Comprueba si ya existe un usuario con ese correo. Se usa antes de
     * registrar uno nuevo para evitar duplicados.
     *
     * @param correo correo a comprobar.
     * @return true si el correo ya esta en uso, false si esta libre.
     */
    boolean existsByCorreo(String correo);
}
