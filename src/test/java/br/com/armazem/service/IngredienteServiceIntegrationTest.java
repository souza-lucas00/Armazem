package br.com.armazem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.armazem.dto.IngredienteRequest;
import br.com.armazem.entity.Compartimento;
import br.com.armazem.entity.Ingrediente;
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.repository.CompartimentoRepository;
import br.com.armazem.repository.HistoricoRepository;
import br.com.armazem.repository.IngredienteRepository;


// Inicializa o contexto completo do Spring Boot para o teste.
@SpringBootTest

// Mantém o teste em uma transação que será revertida ao terminar.
@Transactional
class IngredienteServiceIntegrationTest {

    // Injeta o serviço real de cadastro.
    @Autowired
    private IngredienteService ingredienteService;

    // Injeta o repositório real de ingredientes.
    @Autowired
    private IngredienteRepository ingredienteRepository;

    // Injeta o repositório real de compartimentos.
    @Autowired
    private CompartimentoRepository compartimentoRepository;

    // Injeta o repositório real de histórico.
    @Autowired
    private HistoricoRepository historicoRepository;

    // Verifica se o cadastro atualiza todas as tabelas necessárias.
    @Test
    void devePersistirIngredienteEstoqueEHistorico() {

        // Cria um compartimento exclusivo para este teste.
        Compartimento compartimento = new Compartimento();

        // Define um código diferente dos cinco compartimentos iniciais.
        compartimento.setCodigo("TESTE");

        // Inicializa o estoque com zero.
        compartimento.setQuantidadeAtual(BigDecimal.ZERO);

        // Salva o compartimento no H2.
        compartimento = compartimentoRepository.save(compartimento);

        // Guarda o identificador gerado pelo banco.
        Long compartimentoId = compartimento.getId();

        // Conta os históricos existentes antes do cadastro.
        long historicosAntes = historicoRepository.count();

        // Cria uma requisição para cadastrar 100 kg de arroz.
        IngredienteRequest request = new IngredienteRequest(
                "Arroz",
                TipoIngrediente.SECOS,
                new BigDecimal("100"),
                compartimentoId,
                "João"
        );

        // Executa o cadastro utilizando o Service real.
        Ingrediente ingrediente = ingredienteService.cadastrar(request);

        // Confirma que o ingrediente recebeu um ID.
        assertNotNull(ingrediente.getId());

        // Recupera o ingrediente diretamente do banco.
        Ingrediente ingredienteSalvo =
                ingredienteRepository.findById(ingrediente.getId())
                        .orElseThrow();

        // Confirma que o ingrediente foi cadastrado com a quantidade correta.
        assertEquals(
                0,
                ingredienteSalvo.getQuantidade()
                        .compareTo(new BigDecimal("100"))
        );

        // Consulta o compartimento atualizado.
        Compartimento compartimentoAtualizado =
                compartimentoRepository.findById(compartimentoId)
                        .orElseThrow();

        // Confirma que o estoque do compartimento passou para 100 kg.
        assertEquals(
                0,
                compartimentoAtualizado.getQuantidadeAtual()
                        .compareTo(new BigDecimal("100"))
        );

        // Confirma que o tipo armazenado passou a ser SECOS.
        assertEquals(
                TipoIngrediente.SECOS,
                compartimentoAtualizado.getTipoAtual()
        );

        // Confirma que um novo registro de histórico foi criado.
        assertEquals(
                historicosAntes + 1,
                historicoRepository.count()
        );
    }
}