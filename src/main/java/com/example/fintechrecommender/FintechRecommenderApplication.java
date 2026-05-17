package com.example.fintechrecommender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de arranque de la aplicacion Spring Boot.
 *
 * Es el punto de entrada del backend: cuando se ejecuta el JAR (o se
 * arranca desde el IDE), Spring escanea los componentes (controllers,
 * services, repositories), levanta el servidor embebido y deja la API
 * REST disponible.
 */
@SpringBootApplication
public class FintechRecommenderApplication {

	/**
	 * Metodo main estandar que arranca el contexto de Spring.
	 *
	 * @param args argumentos de linea de comandos pasados al ejecutar la aplicacion.
	 */
	public static void main(String[] args) {
		SpringApplication.run(FintechRecommenderApplication.class, args);
	}
}

