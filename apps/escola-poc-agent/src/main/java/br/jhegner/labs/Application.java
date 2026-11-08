package br.jhegner.labs;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        var app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    /**
     * Exibido uma vez após todos os beans estarem prontos
     * (Ollama conectado, MCP Server conectado).
     */
    @Bean
    public ApplicationRunner banner() {
        return args -> {
            String R = "\u001B[0m", B = "\u001B[1m", C = "\u001B[36m", D = "\u001B[2m";
            System.out.println(C + B + """

            ╔══════════════════════════════════════════╗
            ║      Assistente da Escola Labs v2.0      ║
            ║   Spring Boot 4.1 · Spring AI 2.0.1      ║
            ╚══════════════════════════════════════════╝
            """ + R);
            System.out.println(D + "  Digite " + R + B + "ajuda" + R +
                    D + " para ver exemplos de perguntas." + R);
            System.out.println(D + "  Digite " + R + B + "p \"Sua pergunta\"" + R +
                    D + " para começar.\n" + R);
        };
    }
}
