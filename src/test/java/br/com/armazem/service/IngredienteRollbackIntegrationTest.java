package br.com.armazem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.armazem.dto.IngredienteRequest;
import br.com.armazem.entity.Compartimento;
import br.com.armazem.entity.Historico;
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.repository.CompartimentoRepository;
import br.com.armazem.repository.HistoricoRepository;
import br.com.armazem.repository.IngredienteRepository;
import org.springframework.test.annotation.DirtiesContext;

// Inicializa o contexto real do Spring Boot e o banco H2.
@SpringBootTest
// Cria um contexto novo antes desta classe de teste.
// Isso evita reutilizar o contexto modificado por outro teste.
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class IngredienteRollbackIntegrationTest {

    // Injeta o serviço real que possui o método @Transactional.
    @Autowired
    private IngredienteService ingredienteService;

    // Utiliza o repositório real de ingredientes.
    @Autowired
    private IngredienteRepository ingredienteRepository;

    // Utiliza o repositório real de compartimentos.
    @Autowired
    private CompartimentoRepository compartimentoRepository;

    // Substitui apenas o repositório de histórico por um mock.
    // Isso permite simular uma falha no momento do salvamento.
    @MockitoBean
    private HistoricoRepository historicoRepository;

    // Verifica se uma falha no histórico desfaz o cadastro e o estoque.
    @Test
    void deveDesfazerTransacaoQuandoHistoricoFalhar() {

        // Busca C1, que já foi criado pelo DataInitializer.
        Compartimento compartimento =
                compartimentoRepository.findByCodigo("C1")
                        .orElseThrow();

                // Verifica se C1 está vazio e ainda não possui um tipo definido.
        assertEquals(0, compartimento.getQuantidadeAtual().compareTo(BigDecimal.ZERO));
        assertEquals(null, compartimento.getTipoAtual());
        assertEquals(null, compartimento.getDataUltimaUtilizacao());

// Guarda o ID para realizar o cadastro.

        // Guarda o ID do compartimento.
        Long compartimentoId = compartimento.getId();

        // Guarda o saldo do compartimento antes da tentativa de cadastro.
        BigDecimal saldoAntes = compartimento.getQuantidadeAtual();

        // Guarda o tipo associado ao compartimento.
        TipoIngrediente tipoAntes = compartimento.getTipoAtual();

        // Guarda a data da última utilização.
        LocalDate dataAntes = compartimento.getDataUltimaUtilizacao();

        // Conta quantos ingredientes existem antes da operação.
        long ingredientesAntes = ingredienteRepository.count();

        // Configura o mock para lançar uma exceção ao salvar histórico.
        doThrow(new RuntimeException("Falha simulada no histórico"))
                .when(historicoRepository)
                .save(any(Historico.class));

        // Monta uma requisição para cadastrar 100 kg de arroz.
        IngredienteRequest request = new IngredienteRequest(
                "Arroz",
                TipoIngrediente.SECOS,
                new BigDecimal("100"),
                compartimentoId,
                "João"
        );

        // Confirma que a tentativa de cadastro lança a falha simulada.
        assertThrows(RuntimeException.class, () -> ingredienteService.cadastrar(request));

        // Confirma que o serviço tentou registrar o histórico.
        verify(historicoRepository).save(any(Historico.class));

        // Consulta novamente o total de ingredientes no banco.
        long ingredientesDepois = ingredienteRepository.count();

        // Confirma que nenhum novo ingrediente permaneceu salvo.
        assertEquals(ingredientesAntes, ingredientesDepois);

        // Busca novamente o compartimento após a falha.
        Compartimento compartimentoDepois = compartimentoRepository.findById(compartimentoId).orElseThrow();

        // Confirma que o saldo permaneceu igual ao valor anterior.
        assertEquals(0, compartimentoDepois.getQuantidadeAtual().compareTo(saldoAntes));

        // Confirma que o tipo do compartimento não foi alterado.
        assertEquals(tipoAntes, compartimentoDepois.getTipoAtual());

        // Confirma que a data de utilização não foi alterada.
        assertEquals(dataAntes, compartimentoDepois.getDataUltimaUtilizacao());
    }
}