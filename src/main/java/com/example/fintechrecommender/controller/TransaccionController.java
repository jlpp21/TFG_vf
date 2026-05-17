package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.model.Transaccion;
import com.example.fintechrecommender.model.Cliente;
import com.example.fintechrecommender.repository.TransaccionRepository;
import com.example.fintechrecommender.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST legacy con CRUD de Transaccion bajo /api/transacciones.
 *
 * Pertenece al modulo anterior (junto con ClienteController). Habla
 * directamente con los repositorios sin capa de servicio. Cuando se crea
 * o actualiza una transaccion, vincula el cliente buscandolo por id si
 * viene incluido en el cuerpo. Permite peticiones desde cualquier origen.
 */
@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "*")
public class TransaccionController {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Devuelve todas las transacciones de la base de datos.
     *
     * @return lista de transacciones (vacia si no hay ninguna).
     */
    @GetMapping
    public List<Transaccion> getAllTransacciones() {
        return transaccionRepository.findAll();
    }

    /**
     * Busca una transaccion por su id.
     *
     * @param id id de la transaccion.
     * @return Optional con la transaccion si existe, vacio en caso contrario.
     */
    @GetMapping("/{id}")
    public Optional<Transaccion> getTransaccionById(@PathVariable Long id) {
        return transaccionRepository.findById(id);
    }

    /**
     * Crea una transaccion nueva. Si en el cuerpo viene un cliente con id,
     * la enlaza con el cliente real cargado de base de datos.
     *
     * @param transaccion datos de la transaccion a guardar.
     * @return la transaccion persistida (con id generado).
     */
    @PostMapping
    public Transaccion createTransaccion(@RequestBody Transaccion transaccion) {
        // Vincular cliente si viene el id
        if (transaccion.getCliente() != null && transaccion.getCliente().getId() != null) {
            Optional<Cliente> clienteOpt = clienteRepository.findById(transaccion.getCliente().getId());
            clienteOpt.ifPresent(transaccion::setCliente);
        }
        return transaccionRepository.save(transaccion);
    }

    /**
     * Actualiza una transaccion existente. Sobrescribe el id con el del path
     * y, si viene cliente con id, lo recarga de base de datos.
     *
     * @param id          id de la transaccion a actualizar.
     * @param transaccion nuevos datos de la transaccion.
     * @return la transaccion actualizada.
     */
    @PutMapping("/{id}")
    public Transaccion updateTransaccion(@PathVariable Long id, @RequestBody Transaccion transaccion) {
        transaccion.setId(id);
        // Vincular cliente si viene el id
        if (transaccion.getCliente() != null && transaccion.getCliente().getId() != null) {
            Optional<Cliente> clienteOpt = clienteRepository.findById(transaccion.getCliente().getId());
            clienteOpt.ifPresent(transaccion::setCliente);
        }
        return transaccionRepository.save(transaccion);
    }

    /**
     * Borra una transaccion por su id.
     *
     * @param id id de la transaccion a eliminar.
     */
    @DeleteMapping("/{id}")
    public void deleteTransaccion(@PathVariable Long id) {
        transaccionRepository.deleteById(id);
    }
}

