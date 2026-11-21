package br.com.armazem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.armazem.dto.HistoricoResponse;
import br.com.armazem.service.HistoricoService;

// Define a classe como um Controller REST.
@RestController

// Estabelece a URL base do histórico.
@RequestMapping("/historico")
public class HistoricoController {

    // Serviço responsável pelas consultas do histórico.
    private final HistoricoService historicoService;

    // Recebe o serviço por injeção de dependência.
    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    // Mapeia as requisições GET para /historico.
    @GetMapping
    public ResponseEntity<List<HistoricoResponse>> listar(

            // Recebe o critério de ordenação, usando date como padrão.
            @RequestParam(defaultValue = "date") String sortBy,

            // Recebe a direção da ordenação, usando asc como padrão.
            @RequestParam(defaultValue = "asc") String order) {

        // Consulta o histórico com os parâmetros informados.
        List<HistoricoResponse> resposta =
                historicoService.listar(sortBy, order);

        // Devolve os registros encontrados com HTTP 200 OK.
        return ResponseEntity.ok(resposta);
    }
}