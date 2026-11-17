package br.com.cupcakes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CupcakesApplication {

    public static void main(String[] args) {
        mostrarBancoConfigurado();
        SpringApplication.run(CupcakesApplication.class, args);
    }

    /** Diagnóstico de hospedagem: mostra no log qual banco será usado (sem a senha). */
    private static void mostrarBancoConfigurado() {
        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        boolean temSenha = System.getenv("DB_PASSWORD") != null && !System.getenv("DB_PASSWORD").isBlank();
        System.out.println("==================== BANCO DE DADOS ====================");
        System.out.println("DB_URL      = " + (url == null ? "(NÃO DEFINIDA -> usando localhost do application.properties)" : url));
        System.out.println("DB_USER     = " + (usuario == null ? "(NÃO DEFINIDA -> root)" : usuario));
        System.out.println("DB_PASSWORD = " + (temSenha ? "(definida)" : "(NÃO DEFINIDA)"));
        System.out.println("========================================================");
    }
}
