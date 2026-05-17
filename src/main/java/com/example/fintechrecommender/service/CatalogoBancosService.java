package com.example.fintechrecommender.service;

import com.example.fintechrecommender.dto.BancoCatalogoDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que provee el catalogo estatico de bancos espanoles soportados.
 *
 * Sirve como fuente unica de verdad para los logos, dominios y colores
 * corporativos de cada banco. La lista esta hardcodeada (no se carga de
 * base de datos) porque cambia muy rara vez.
 */
@Service
public class CatalogoBancosService {

    /** Prefijo de Wikimedia Commons para construir las URLs de logo. */
    private static final String WIKIMEDIA = "https://commons.wikimedia.org/wiki/Special:FilePath/";

    /** Lista inmutable de bancos disponibles para conectar. */
    private static final List<BancoCatalogoDto> BANCOS = Arrays.asList(
        new BancoCatalogoDto("santander", "Banco Santander", "santander.com",    "#ec0000", WIKIMEDIA + "Banco_Santander_Logotipo.svg"),
        new BancoCatalogoDto("bbva",      "BBVA",            "bbva.com",         "#004481", WIKIMEDIA + "BBVA_2019.svg"),
        new BancoCatalogoDto("caixabank", "CaixaBank",       "caixabank.com",    "#007eae", WIKIMEDIA + "Logo_CaixaBank.svg"),
        new BancoCatalogoDto("sabadell",  "Banco Sabadell",  "bancsabadell.com", "#00529b", WIKIMEDIA + "Banco-Sabadell-Logo.svg"),
        new BancoCatalogoDto("bankinter", "Bankinter",       "bankinter.com",    "#ff6900", WIKIMEDIA + "Bankinter.svg"),
        new BancoCatalogoDto("ing",       "ING España",      "ing.es",           "#ff6200", WIKIMEDIA + "ING_Group_%28wordmark%29.svg"),
        new BancoCatalogoDto("openbank",  "Openbank",        "openbank.es",      "#ec0000", WIKIMEDIA + "Openbank_logoOK_220x1665px.gif"),
        new BancoCatalogoDto("unicaja",   "Unicaja Banco",   "unicajabanco.es",  "#00833f", WIKIMEDIA + "Logotipo_Unicaja_Banco.svg"),
        new BancoCatalogoDto("kutxabank", "Kutxabank",       "kutxabank.es",     "#0072ce", WIKIMEDIA + "Kutxabank.svg"),
        new BancoCatalogoDto("abanca",    "Abanca",          "abanca.com",       "#00a4e4", WIKIMEDIA + "Abanca_logo.svg")
    );

    /**
     * Devuelve la lista completa de bancos del catalogo.
     *
     * @return lista inmutable de BancoCatalogoDto.
     */
    public List<BancoCatalogoDto> listar() {
        return BANCOS;
    }

    /**
     * Busca un banco por su codigo (case-insensitive).
     *
     * @param codigo codigo del banco (ej: "bbva", "santander").
     * @return Optional con el banco si existe en el catalogo, vacio si no.
     */
    public Optional<BancoCatalogoDto> buscar(String codigo) {
        if (codigo == null) return Optional.empty();
        return BANCOS.stream().filter(b -> b.getCodigo().equalsIgnoreCase(codigo)).findFirst();
    }
}
