package br.com.armazem.controller;

import java.math.BigDecimal;
import java.util.List;
import br.com.armazem.dto.CompartimentoResponse;
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.service.CompartimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Define o Controller como responsável pelas rotas de compartimentos.
@RestController
// Define /compartimentos como caminho base.
@RequestMapping("/compartimentos")
public class CompartimentoController {

    // Referência ao serviço de compartimentos.
    private final CompartimentoService compartimentoService;

    // Recebe o serviço por injeção de dependência.
    public CompartimentoController(CompartimentoService compartimentoService) {

        // Armazena o serviço recebido.
        this.compartimentoService = compartimentoService;
    }

    // Mapeia a consulta de compartimentos disponíveis.
    @GetMapping("/disponiveis")
    public ResponseEntity<List<CompartimentoResponse>> buscarDisponiveis(

            // Lê a quantidade informada na URL.
            @RequestParam BigDecimal quantidade,

            // Lê o tipo informado na URL.
            @RequestParam TipoIngrediente tipo) {

        // Consulta os compartimentos disponíveis no Service.
        List<CompartimentoResponse> resposta =
                compartimentoService.buscarDisponiveis(quantidade, tipo)

                        // Percorre os compartimentos encontrados.
                        .stream()

                        // Converte cada compartimento em DTO.
                        .map(compartimento ->
                                CompartimentoResponse.fromEntity(
                                        compartimento, tipo
                                )
                        )

                        // Reúne os DTOs em uma lista.
                        .toList();

        // Retorna a lista com HTTP 200 OK.
        return ResponseEntity.ok(resposta);
    }
    // Mapeia requisições GET para /compartimentos/disponiveis-para-venda.
    @GetMapping("/disponiveis-para-venda")
    public ResponseEntity<List<CompartimentoResponse>> buscarDisponiveisParaVenda(

            // Recebe o tipo de ingrediente informado na URL.
            @RequestParam TipoIngrediente tipo) {

        // Consulta os compartimentos que possuem estoque desse tipo.
        List<CompartimentoResponse> resposta =
                compartimentoService.buscarDisponiveisParaVenda(tipo)

                        // Inicia a conversão dos compartimentos para DTOs.
                        .stream()

                        // Converte cada compartimento para o formato de resposta.
                        .map(compartimento ->
                                CompartimentoResponse.fromEntity(
                                        compartimento, tipo
                                )
                        )

                        // Reúne os resultados em uma lista.
                        .toList();

        // Retorna a lista com HTTP 200 OK.
        return ResponseEntity.ok(resposta);
    }
}