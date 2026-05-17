package com.example.fintechrecommender.service;

import com.example.fintechrecommender.dto.BancoCatalogoDto;
import com.example.fintechrecommender.dto.CuentaMockDto;
import com.example.fintechrecommender.model.BancoConectado;
import com.example.fintechrecommender.model.Movimiento;
import com.example.fintechrecommender.model.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que simula los pasos de autenticacion PSD2 / Open Banking
 * en un entorno sandbox.
 *
 * Imita el flujo: validar credenciales -> validar codigo SCA -> mostrar
 * cuentas mock con saldos. Las credenciales y el SCA son ficticios (no
 * se valida nada real, solo el formato y un codigo SCA fijo). Los saldos
 * y movimientos se generan con MovimientoGeneratorService y se cachean
 * en MovimientoPreviewCache para que coincidan al confirmar la conexion.
 */
@Service
public class SandboxBancoService {

    /** Codigo SCA fijo aceptado en sandbox. */
    private static final String CODIGO_SCA_VALIDO = "123456";

    /** Longitud minima del usuario y la contrasena del sandbox. */
    private static final int LONGITUD_MINIMA = 4;

    private final CatalogoBancosService catalogo;
    private final MovimientoGeneratorService generator;
    private final MovimientoPreviewCache previewCache;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param catalogo     catalogo de bancos soportados.
     * @param generator    generador de movimientos sinteticos.
     * @param previewCache cache de movimientos del wizard.
     */
    public SandboxBancoService(CatalogoBancosService catalogo,
                               MovimientoGeneratorService generator,
                               MovimientoPreviewCache previewCache) {
        this.catalogo = catalogo;
        this.generator = generator;
        this.previewCache = previewCache;
    }

    /**
     * Valida que el usuario y la contrasena del sandbox tengan al menos
     * la longitud minima. No valida contra ningun sistema real.
     *
     * @param usuario  usuario simulado del banco.
     * @param password contrasena simulada.
     * @throws IllegalArgumentException si alguno es null o demasiado corto.
     */
    public void validarCredenciales(String usuario, String password) {
        if (usuario == null || usuario.trim().length() < LONGITUD_MINIMA) {
            throw new IllegalArgumentException("Usuario o contrasena invalidos (minimo " + LONGITUD_MINIMA + " caracteres)");
        }
        if (password == null || password.length() < LONGITUD_MINIMA) {
            throw new IllegalArgumentException("Usuario o contrasena invalidos (minimo " + LONGITUD_MINIMA + " caracteres)");
        }
    }

    /**
     * Valida el codigo SCA (autenticacion fuerte) recibido del sandbox.
     * Solo acepta el codigo "123456".
     *
     * @param codigo codigo de 6 digitos.
     * @throws IllegalArgumentException si el formato no es correcto o el codigo no coincide.
     */
    public void validarCodigoSCA(String codigo) {
        if (codigo == null || !codigo.matches("\\d{6}")) {
            throw new IllegalArgumentException("El codigo debe tener 6 digitos");
        }
        if (!CODIGO_SCA_VALIDO.equals(codigo)) {
            throw new IllegalArgumentException("Codigo incorrecto. En sandbox usa " + CODIGO_SCA_VALIDO);
        }
    }

    /**
     * Genera y devuelve la lista de cuentas mock del banco para el usuario.
     * Para cada tipo (CORRIENTE, AHORRO, CREDITO, PRESTAMO) genera los
     * movimientos sinteticos, los cachea en preview y calcula el saldo
     * correspondiente. Antes de empezar limpia cualquier preview anterior
     * de ese mismo banco para evitar mezclas.
     *
     * @param usuario     usuario autenticado.
     * @param bancoCodigo codigo del banco a previsualizar.
     * @return lista con 4 cuentas mock (una por cada tipo).
     * @throws IllegalArgumentException si el banco no esta soportado o el usuario no tiene perfil financiero.
     */
    public List<CuentaMockDto> cuentasMock(Usuario usuario, String bancoCodigo) {
        Optional<BancoCatalogoDto> b = catalogo.buscar(bancoCodigo);
        if (!b.isPresent()) {
            throw new IllegalArgumentException("Banco no soportado: " + bancoCodigo);
        }
        if (usuario.getPerfilFinanciero() == null) {
            throw new IllegalArgumentException("El usuario no tiene perfil financiero");
        }

        BancoCatalogoDto banco = b.get();
        previewCache.limpiar(usuario.getId(), bancoCodigo);

        String prefijo = bancoCodigo.length() >= 4
                ? bancoCodigo.substring(0, 4).toUpperCase()
                : (bancoCodigo + "XXXX").substring(0, 4).toUpperCase();

        BigDecimal saldoCorriente = generarYCachear(usuario, banco, "CORRIENTE");
        BigDecimal saldoAhorro = generarYCachear(usuario, banco, "AHORRO");
        BigDecimal saldoCredito = generarYCachear(usuario, banco, "CREDITO");
        BigDecimal saldoPrestamo = generarYCachear(usuario, banco, "PRESTAMO");

        return Arrays.asList(
            new CuentaMockDto("CORRIENTE", "Cuenta Corriente",
                    "ES12 " + prefijo + " XXXX 1234",
                    saldoCorriente,
                    "Recibe nomina, paga gastos del dia a dia."),
            new CuentaMockDto("AHORRO", "Cuenta de Ahorro",
                    "ES34 " + prefijo + " XXXX 5678",
                    saldoAhorro,
                    "Aportaciones mensuales y liquidacion de intereses."),
            new CuentaMockDto("CREDITO", "Tarjeta de credito",
                    "**** **** **** 9921",
                    saldoCredito,
                    "Compras durante el mes, liquidacion al final."),
            new CuentaMockDto("PRESTAMO", "Prestamo personal",
                    "PR " + prefijo + " 4421",
                    saldoPrestamo,
                    "Capital pendiente y cuotas mensuales del prestamo.")
        );
    }

    /**
     * Genera los movimientos para un tipo de cuenta concreto, los guarda
     * en el cache del preview y calcula el saldo total.
     *
     * @param usuario    usuario autenticado.
     * @param banco      banco del catalogo.
     * @param tipoCuenta tipo de cuenta a generar (CORRIENTE, AHORRO, CREDITO, PRESTAMO).
     * @return saldo final tras sumar todos los movimientos generados.
     */
    private BigDecimal generarYCachear(Usuario usuario, BancoCatalogoDto banco, String tipoCuenta) {
        BancoConectado transient_ = new BancoConectado();
        transient_.setUsuario(usuario);
        transient_.setBancoCodigo(banco.getCodigo());
        transient_.setNombre(banco.getNombre());
        transient_.setDominio(banco.getDominio());
        transient_.setColor(banco.getColor());
        transient_.setLogoUrl(banco.getLogoUrl());
        transient_.setTipoCuenta(tipoCuenta);

        List<Movimiento> movs = generator.generar(transient_, usuario.getPerfilFinanciero());
        previewCache.guardar(usuario.getId(), banco.getCodigo(), tipoCuenta, new ArrayList<>(movs));

        BigDecimal balance = BigDecimal.ZERO;
        for (Movimiento m : movs) balance = balance.add(m.getMonto());
        return balance;
    }
}
