package br.com.armazem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import br.com.armazem.dto.IngredienteRequest;
import br.com.armazem.dto.IngredienteResponse;
import br.com.armazem.service.IngredienteService;

import jakarta.validation.Valid;
import java.util.List;

// Permite retornar um mapa no JSON.
import java.util.Map;

// Representa os volumes com precisão decimal.
import java.math.BigDecimal;

// Identifica os tipos de ingredientes.
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.dto.SaidaIngredienteRequest;

// Identifica a classe como um Controller REST.
@RestController

// Define a rota base dos ingredientes.
@RequestMapping("/ingredientes")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    public IngredienteController(IngredienteService ingredienteService) {
        this.ingredienteService = ingredienteService;
    }

    // Recebe uma requisição para cadastrar ingredientes.
    @PostMapping
    public ResponseEntity<IngredienteResponse> cadastrar(

            // Converte o JSON para o DTO e valida seus campos.
            @Valid @RequestBody IngredienteRequest request) {

        // Executa o cadastro e converte a entidade para o DTO de resposta.
        IngredienteResponse response = IngredienteResponse.fromEntity(
                ingredienteService.cadastrar(request)
        );

        // Retorna HTTP 201 com o ingrediente cadastrado.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<List<IngredienteResponse>> listarTodos() {

        return ResponseEntity.ok(
                ingredienteService.listarTodos()
        );
    }
    // Mapeia requisições GET que possuem um ID na URL.
    // Exemplo: GET /ingredientes/1
    @GetMapping("/{id}")
    public ResponseEntity<IngredienteResponse> buscarPorId(

            // Captura o valor de {id} da URL e o converte para Long.
            @PathVariable Long id) {

        // Chama o Service para consultar o ingrediente.
        IngredienteResponse response = ingredienteService.buscarPorId(id);

        // Retorna o ingrediente encontrado com HTTP 200 OK.
        return ResponseEntity.ok(response);
    }

    // Mapeia requisições GET para /ingredientes/volume.
    @GetMapping("/volume")
    public ResponseEntity<Map<TipoIngrediente, BigDecimal>> consultarVolume() {

        // Solicita ao Service os volumes totais por tipo.
        Map<TipoIngrediente, BigDecimal> volumes =
                ingredienteService.consultarVolumePorTipo();

        // Retorna os volumes em JSON com HTTP 200 OK.
        return ResponseEntity.ok(volumes);
    }
    // Define a rota POST /ingredientes/{id}/saida.
    // Mapeia o endpoint responsável por registrar uma saída.
    @PostMapping("/{id}/saida")
    public ResponseEntity<IngredienteResponse> registrarSaida(

            // Recebe o identificador do ingrediente pela URL.
            @PathVariable Long id,

            // Converte o JSON recebido para o DTO e executa suas validações.
            @Valid @RequestBody SaidaIngredienteRequest request) {

        // Executa a operação de saída no Service.
        IngredienteResponse response =
                ingredienteService.registrarSaida(id, request);

        // Retorna o ingrediente atualizado com HTTP 200 OK.
        return ResponseEntity.ok(response);
    }
}