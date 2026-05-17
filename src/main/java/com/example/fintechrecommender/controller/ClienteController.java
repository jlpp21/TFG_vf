package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.model.Cliente;
import com.example.fintechrecommender.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST legacy que expone un CRUD basico de Cliente bajo /api/clientes.
 *
 * Pertenece al modulo anterior (junto con Transaccion). Habla directamente
 * con el ClienteRepository sin pasar por una capa de servicio. Permite
 * llamadas desde cualquier origen via @CrossOrigin.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Devuelve todos los clientes guardados en la base de datos.
     *
     * @return lista de clientes (vacia si no hay ninguno).
     */
    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    /**
     * Busca un cliente por su id.
     *
     * @param id id del cliente.
     * @return Optional con el cliente si existe, vacio en caso contrario.
     */
    @GetMapping("/{id}")
    public Optional<Cliente> getClienteById(@PathVariable Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Crea un cliente nuevo.
     *
     * @param cliente datos del cliente a guardar.
     * @return el cliente persistido (con su id generado).
     */
    @PostMapping
    public Cliente createCliente(@RequestBody Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Actualiza un cliente existente. Sobrescribe el id con el del path para
     * asegurarse de que actualiza el registro correcto.
     *
     * @param id      id del cliente a actualizar.
     * @param cliente nuevos datos del cliente.
     * @return el cliente ya actualizado en base de datos.
     */
    @PutMapping("/{id}")
    public Cliente updateCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        cliente.setId(id);
        return clienteRepository.save(cliente);
    }

    /**
     * Borra un cliente por su id.
     *
     * @param id id del cliente a eliminar.
     */
    @DeleteMapping("/{id}")
    public void deleteCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
    }
}


