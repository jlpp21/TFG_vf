package com.example.fintechrecommender.repository;

import com.example.fintechrecommender.model.BancoConectado;
import com.example.fintechrecommender.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad BancoConectado.
 *
 * Hereda de JpaRepository, asi que ya dispone de los metodos basicos
 * (save, findById, findAll, deleteById, etc.). Spring Data JPA genera la
 * implementacion de los metodos derivados a partir del nombre.
 */
public interface BancoConectadoRepository extends JpaRepository<BancoConectado, Long> {

    /**
     * Busca todas las conexiones bancarias de un usuario ordenadas por fecha
     * de conexion ascendente (las mas antiguas primero).
     *
     * @param usuario usuario propietario de las conexiones.
     * @return lista de conexiones del usuario (vacia si no tiene ninguna).
     */
    List<BancoConectado> findByUsuarioOrderByFechaConexionAsc(Usuario usuario);

    /**
     * Busca una conexion concreta por id, comprobando que pertenece al usuario.
     * Sirve para evitar que un usuario acceda a conexiones de otro.
     *
     * @param id      id de la conexion.
     * @param usuario usuario que dice ser propietario.
     * @return Optional con la conexion si existe y es del usuario, vacio en caso contrario.
     */
    Optional<BancoConectado> findByIdAndUsuario(Long id, Usuario usuario);

    /**
     * Comprueba si el usuario ya tiene conectado ese banco con el mismo tipo de cuenta.
     * Sirve para evitar duplicados antes de insertar una nueva conexion.
     *
     * @param usuario     usuario a comprobar.
     * @param bancoCodigo codigo del banco.
     * @param tipoCuenta  tipo de cuenta (CORRIENTE, AHORRO, etc.).
     * @return true si ya existe la conexion, false si no.
     */
    boolean existsByUsuarioAndBancoCodigoAndTipoCuenta(Usuario usuario, String bancoCodigo, String tipoCuenta);
}
