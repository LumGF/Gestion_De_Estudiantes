// Paquete renombrado: com.t2lguevara.backend → com.colegio.gestionacceso
package com.colegio.gestionacceso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.colegio.gestionacceso.config.JwtProperties;
import com.colegio.gestionacceso.config.WhatsAppProperties;

@SpringBootApplication
@EnableConfigurationProperties({WhatsAppProperties.class, JwtProperties.class})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
