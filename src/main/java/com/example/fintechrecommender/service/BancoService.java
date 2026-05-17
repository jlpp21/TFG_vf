package com.example.fintechrecommender.service;

import com.example.fintechrecommender.dto.BancoDto;
import com.example.fintechrecommender.dto.ConectarBancoRequest;
import com.example.fintechrecommender.dto.MovimientoEntradaDto;
import com.example.fintechrecommender.model.BancoConectado;
import com.example.fintechrecommender.model.Movimiento;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.repository.BancoConectadoRepository;
import com.example.fintechrecommender.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona las conexiones bancarias activas del usuario.
 *
 * Cubre el ciclo de vida de un BancoConectado: listar, conectar uno
 * nuevo (con sus movimientos iniciales), anyadir movimientos y
 * desconectar. Cuando se conecta un banco sin movimientos previos,
 * intenta reutilizar los que se cachearon en el preview del wizard
 * (MovimientoPreviewCache); si no hay nada cacheado, los genera con
 * MovimientoGeneratorService.
 */
@Service
public class BancoService {

    private final BancoConectadoRepository bancoRepo;
    private final MovimientoRepository movRepo;
    private final MovimientoGeneratorService generator;
    private final MovimientoPreviewCache previewCache;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param bancoRepo    repositorio de BancoConectado.
     * @param movRepo      repositorio de Movimiento.
     * @param generator    generador de movimientos sinteticos.
     * @param previewCache cache de movimientos generados durante el wizard.
     */
    public BancoService(BancoConectadoRepository bancoRepo,
                        MovimientoRepository movRepo,
                        MovimientoGeneratorService generator,
                        MovimientoPreviewCache previewCache) {
        this.bancoRepo = bancoRepo;
        this.movRepo = movRepo;
        this.generator = generator;
        this.previewCache = previewCache;
    }

    /**
     * Lista los bancos conectados del usuario en orden de fecha de conexion ascendente.
     *
     * @param usuario usuario propietario.
     * @return lista de BancoDto (vacia si el usuario no tiene ningun banco).
     */
    public List<BancoDto> listar(Usuario usuario) {
        return bancoRepo.findByUsuarioOrderByFechaConexionAsc(usuario)
                .stream()
                .map(BancoDto::from)
                .collect(Collectors.toList());
    }

    /** Tipos de cuenta aceptados al conectar un banco. */
    private static final List<String> TIPOS_VALIDOS = Arrays.asList("CORRIENTE", "AHORRO", "CREDITO", "PRESTAMO");

    /**
     * Conecta un banco al usuario y persiste sus movimientos iniciales.
     * Si el cliente envia movimientos los acepta tal cual; si no, intenta
     * usar los del cache del preview para que el saldo coincida con el
     * mostrado durante el wizard. En ultimo caso los genera con el
     * MovimientoGeneratorService.
     *
     * @param usuario usuario que conecta el banco.
     * @param req     datos del banco y movimientos opcionales.
     * @return BancoDto con la conexion creada.
     * @throws IllegalArgumentException si el tipo de cuenta no es valido o si la combinacion banco+tipo ya existe para el usuario.
     */
    @Transactional
    public BancoDto conectar(Usuario usuario, ConectarBancoRequest req) {
        String tipo = req.getTipoCuenta() != null ? req.getTipoCuenta().toUpperCase() : "CORRIENTE";
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de cuenta no valido: " + tipo);
        }
        if (bancoRepo.existsByUsuarioAndBancoCodigoAndTipoCuenta(usuario, req.getBancoCodigo(), tipo)) {
            throw new IllegalArgumentException("Ya tienes este tipo de cuenta conectado para este banco");
        }

        BancoConectado banco = new BancoConectado();
        banco.setUsuario(usuario);
        banco.setBancoCodigo(req.getBancoCodigo());
        banco.setNombre(req.getNombre());
        banco.setDominio(req.getDominio());
        banco.setColor(req.getColor());
        banco.setLogoUrl(req.getLogoUrl());
        banco.setTipoCuenta(tipo);
        String numero = req.getNumeroFinal();
        if (numero == null || numero.trim().isEmpty()) {
            numero = String.format("%04d", new Random().nextInt(10000));
        }
        banco.setNumeroFinal(numero);
        banco = bancoRepo.save(banco);

        // Si el cliente envia movimientos los aceptamos (caso de import manual).
        // Si no, intentamos reutilizar los que se previsualizaron en el wizard
        // para que el saldo mostrado y el balance final coincidan; en ultimo
        // recurso los generamos de cero.
        if (req.getMovimientos() != null && !req.getMovimientos().isEmpty()) {
            agregarLote(banco, req.getMovimientos());
        } else {
            List<Movimiento> generados = previewCache.consumir(usuario.getId(), req.getBancoCodigo(), tipo);
            if (generados == null) {
                generados = generator.generar(banco, usuario.getPerfilFinanciero());
            } else {
                for (Movimiento m : generados) m.setBanco(banco);
            }
            if (!generados.isEmpty()) movRepo.saveAll(generados);
        }

        return BancoDto.from(banco);
    }

    /**
     * Anyade movimientos a un banco ya conectado del usuario.
     *
     * @param usuario  usuario propietario.
     * @param bancoId  id del banco conectado.
     * @param entradas lista de movimientos a insertar.
     * @return numero de movimientos insertados.
     * @throws IllegalArgumentException si el banco no existe o no pertenece al usuario.
     */
    @Transactional
    public int agregarMovimientos(Usuario usuario, Long bancoId, List<MovimientoEntradaDto> entradas) {
        BancoConectado banco = bancoRepo.findByIdAndUsuario(bancoId, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));
        return agregarLote(banco, entradas);
    }

    /**
     * Convierte un lote de DTOs de entrada en entidades Movimiento y las persiste.
     * Si una entrada no trae fecha usa la actual.
     *
     * @param banco    banco al que se asocian los movimientos.
     * @param entradas lista de movimientos en formato DTO.
     * @return numero de movimientos persistidos.
     */
    private int agregarLote(BancoConectado banco, List<MovimientoEntradaDto> entradas) {
        if (entradas == null || entradas.isEmpty()) return 0;
        List<Movimiento> nuevos = new ArrayList<>(entradas.size());
        for (MovimientoEntradaDto entrada : entradas) {
            Movimiento m = new Movimiento();
            m.setBanco(banco);
            m.setFecha(entrada.getFecha() != null ? entrada.getFecha() : LocalDateTime.now());
            m.setDescripcion(entrada.getDescripcion());
            m.setCategoria(entrada.getCategoria());
            m.setMonto(entrada.getMonto());
            nuevos.add(m);
        }
        movRepo.saveAll(nuevos);
        return nuevos.size();
    }

    /**
     * Desconecta un banco del usuario, borrando la conexion y sus movimientos asociados (cascada JPA).
     *
     * @param usuario usuario propietario.
     * @param id      id del banco conectado a borrar.
     * @throws IllegalArgumentException si el banco no existe o no pertenece al usuario.
     */
    @Transactional
    public void desconectar(Usuario usuario, Long id) {
        BancoConectado banco = bancoRepo.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));
        bancoRepo.delete(banco);
    }
}
