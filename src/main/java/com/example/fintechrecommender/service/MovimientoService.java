package com.example.fintechrecommender.service;

import com.example.fintechrecommender.dto.MovimientoDto;
import com.example.fintechrecommender.dto.PageMovimientosDto;
import com.example.fintechrecommender.model.Movimiento;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que ofrece consultas paginadas de movimientos del usuario.
 *
 * Carga todos los movimientos del usuario en orden cronologico, calcula
 * el balance acumulado tras cada uno y el desglose por tipo de cuenta,
 * aplica los filtros opcionales (descripcion, banco, fecha) y devuelve
 * solo la pagina solicitada en orden de mas reciente a mas antigua.
 */
@Service
public class MovimientoService {

    private final MovimientoRepository movRepo;

    /**
     * Construye el servicio con su repositorio.
     *
     * @param movRepo repositorio de movimientos.
     */
    public MovimientoService(MovimientoRepository movRepo) {
        this.movRepo = movRepo;
    }

    /**
     * Devuelve una pagina de movimientos del usuario ordenada de mas reciente a mas antigua,
     * incluyendo el balance acumulado tras cada movimiento y el balance total global.
     *
     * @param usuario           usuario del que se consultan los movimientos.
     * @param page              numero de pagina (0 = primera).
     * @param size              cantidad de movimientos por pagina.
     * @param descripcionFiltro filtro opcional por texto en la descripcion (puede ser null).
     * @param bancoIdFiltro     filtro opcional por id de banco conectado (puede ser null).
     * @param fechaFiltro       filtro opcional por fecha en formato yyyy-MM-dd (puede ser null).
     * @return PageMovimientosDto con la pagina, el balance total y el desglose por tipo de cuenta.
     */
    @Transactional(readOnly = true)
    public PageMovimientosDto listarPaginado(Usuario usuario,
                                             int page,
                                             int size,
                                             String descripcionFiltro,
                                             Long bancoIdFiltro,
                                             String fechaFiltro) {
        // Cargamos todos los movimientos en orden ascendente para calcular el balance acumulado
        List<Movimiento> todos = movRepo.findAllByUsuarioOrderedAsc(usuario);

        BigDecimal balance = BigDecimal.ZERO;
        Map<String, BigDecimal> desglose = new LinkedHashMap<>();
        desglose.put("CORRIENTE", BigDecimal.ZERO);
        desglose.put("AHORRO", BigDecimal.ZERO);
        desglose.put("CREDITO", BigDecimal.ZERO);
        desglose.put("PRESTAMO", BigDecimal.ZERO);
        List<MovimientoDto> dtos = new ArrayList<>(todos.size());

        for (Movimiento m : todos) {
            balance = balance.add(m.getMonto());
            String tipo = m.getBanco() != null && m.getBanco().getTipoCuenta() != null
                    ? m.getBanco().getTipoCuenta() : "CORRIENTE";
            desglose.merge(tipo, m.getMonto(), BigDecimal::add);
            MovimientoDto dto = aDto(m, balance);
            if (cumpleFiltros(dto, descripcionFiltro, bancoIdFiltro, fechaFiltro)) {
                dtos.add(dto);
            }
        }

        BigDecimal balanceTotal = balance;

        // Invertimos para mostrar los mas recientes arriba
        Collections.reverse(dtos);

        // Aplicamos paginacion
        int total = dtos.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<MovimientoDto> pagina = dtos.subList(from, to);

        return new PageMovimientosDto(pagina, page, size, total, balanceTotal, desglose);
    }

    /**
     * Comprueba si el movimiento pasa los filtros activos.
     *
     * @param dto         movimiento a evaluar.
     * @param descripcion filtro de texto (case-insensitive, puede ser null).
     * @param bancoId     filtro de id de banco (puede ser null).
     * @param fecha       filtro de fecha en formato yyyy-MM-dd (puede ser null).
     * @return true si el movimiento pasa todos los filtros activos.
     */
    private boolean cumpleFiltros(MovimientoDto dto, String descripcion, Long bancoId, String fecha) {
        if (descripcion != null && !descripcion.trim().isEmpty()) {
            if (!dto.getDescripcion().toLowerCase().contains(descripcion.toLowerCase().trim())) {
                return false;
            }
        }
        if (bancoId != null && !bancoId.equals(dto.getBancoId())) {
            return false;
        }
        if (fecha != null && !fecha.isEmpty()) {
            String fechaMov = dto.getFecha().toLocalDate().toString();
            if (!fechaMov.equals(fecha)) return false;
        }
        return true;
    }

    /**
     * Convierte la entidad Movimiento al DTO de salida, anyadiendo el
     * balance acumulado calculado y los datos del banco asociado.
     *
     * @param m                 entidad Movimiento.
     * @param balanceAcumulado  saldo acumulado tras este movimiento.
     * @return MovimientoDto listo para enviar al frontend.
     */
    private MovimientoDto aDto(Movimiento m, BigDecimal balanceAcumulado) {
        MovimientoDto dto = new MovimientoDto();
        dto.setId(m.getId());
        dto.setFecha(m.getFecha());
        dto.setDescripcion(m.getDescripcion());
        dto.setCategoria(m.getCategoria());
        dto.setMonto(m.getMonto());
        dto.setBalanceDespues(balanceAcumulado);
        if (m.getBanco() != null) {
            dto.setBancoId(m.getBanco().getId());
            dto.setBancoCodigo(m.getBanco().getBancoCodigo());
            dto.setBancoNombre(m.getBanco().getNombre());
            dto.setBancoColor(m.getBanco().getColor());
            dto.setBancoDominio(m.getBanco().getDominio());
            dto.setBancoLogoUrl(m.getBanco().getLogoUrl());
            dto.setBancoTipoCuenta(m.getBanco().getTipoCuenta());
            dto.setBancoNumeroFinal(m.getBanco().getNumeroFinal());
        }
        return dto;
    }
}
