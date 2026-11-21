package br.com.armazem.config;

import br.com.armazem.entity.Compartimento;
import br.com.armazem.repository.CompartimentoRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner inicializarCompartimentos(
            CompartimentoRepository repository) {

        return args -> {

            List<String> codigos = List.of(
                    "C1", "C2", "C3", "C4", "C5"
            );

            for (String codigo : codigos) {

                boolean existe = repository
                        .findByCodigo(codigo)
                        .isPresent();

                if (!existe) {
                    Compartimento compartimento = new Compartimento();

                    compartimento.setCodigo(codigo);

                    repository.save(compartimento);
                }
            }
        };
    }
}