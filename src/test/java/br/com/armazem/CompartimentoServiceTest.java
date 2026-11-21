package br.com.armazem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.repository.CompartimentoRepository;
import br.com.armazem.service.CompartimentoService;
import java.time.LocalDate;
import br.com.armazem.entity.Compartimento;
class CompartimentoServiceTest {

    // Simula o repositório, sem acessar o banco de dados.
    @Mock
    private CompartimentoRepository compartimentoRepository;

    // Serviço que será testado.
    private CompartimentoService compartimentoService;

    // Executa antes de cada teste.
    @BeforeEach
    void configurar() {

        // Inicializa os objetos simulados pelo Mockito.
        MockitoAnnotations.openMocks(this);

        // Cria o Service utilizando o repositório simulado.
        compartimentoService =
                new CompartimentoService(compartimentoRepository);
    }

    // Verifica que uma entrada dentro do limite é permitida.
    @Test
    void devePermitirEntradaDentroDaCapacidade() {

        // Define que o compartimento possui 100 kg.
        BigDecimal quantidadeAtual = new BigDecimal("100");

        // Define uma entrada de mais 200 kg.
        BigDecimal quantidadeEntrada = new BigDecimal("200");

        // Confirma que a validação não lança nenhuma exceção.
        assertDoesNotThrow(() ->
                compartimentoService.validarCapacidade(
                        TipoIngrediente.SECOS,
                        quantidadeAtual,
                        quantidadeEntrada
                )
        );
    }

    // Verifica que uma entrada acima do limite é rejeitada.
    @Test
    void deveBloquearEntradaAcimaDaCapacidade() {

        // Define que o compartimento possui 500 kg.
        BigDecimal quantidadeAtual = new BigDecimal("500");

        // Define uma entrada de mais 200 kg.
        BigDecimal quantidadeEntrada = new BigDecimal("200");

        // Confirma que a validação lança a exceção esperada.
        assertThrows(IllegalArgumentException.class, () ->
                compartimentoService.validarCapacidade(
                        TipoIngrediente.SECOS,
                        quantidadeAtual,
                        quantidadeEntrada
                )
        );
    }
    // Verifica que um compartimento vazio e nunca utilizado aceita qualquer tipo.
        @Test
        void devePermitirTipoEmCompartimentoNuncaUtilizado() {

        // Cria um compartimento para representar C1.
        Compartimento compartimento = new Compartimento();

        // Define o código do compartimento.
        compartimento.setCodigo("C1");

        // Define que o compartimento está vazio.
        compartimento.setQuantidadeAtual(BigDecimal.ZERO);

        // Confirma que é permitido armazenar líquidos.
        assertDoesNotThrow(() ->
                compartimentoService.validarTipo(
                        compartimento,
                        TipoIngrediente.LIQUIDOS
                )
        );
        }

        // Verifica que não é permitido misturar tipos no mesmo compartimento.
        @Test
        void deveBloquearMisturaDeTipos() {

        // Cria um compartimento para o teste.
        Compartimento compartimento = new Compartimento();

        // Informa que o compartimento armazena ingredientes secos.
        compartimento.setTipoAtual(TipoIngrediente.SECOS);

        // Define que existem 100 kg armazenados.
        compartimento.setQuantidadeAtual(new BigDecimal("100"));

        // Registra a utilização do compartimento no dia atual.
        compartimento.setDataUltimaUtilizacao(LocalDate.now());

        // Confirma que o sistema bloqueia a entrada de líquidos.
        assertThrows(IllegalArgumentException.class, () ->
                compartimentoService.validarTipo(
                        compartimento,
                        TipoIngrediente.LIQUIDOS
                )
        );
        }

        // Verifica que um compartimento esvaziado hoje não pode trocar de tipo.
        @Test
        void deveBloquearTrocaDeTipoNoMesmoDia() {

        // Cria um compartimento para o teste.
        Compartimento compartimento = new Compartimento();

        // Informa que o último tipo armazenado foi SECOS.
        compartimento.setTipoAtual(TipoIngrediente.SECOS);

        // Simula que todo o estoque foi retirado.
        compartimento.setQuantidadeAtual(BigDecimal.ZERO);

        // Simula uma movimentação realizada hoje.
        compartimento.setDataUltimaUtilizacao(LocalDate.now());

        // Confirma que não é permitido armazenar líquidos hoje.
        assertThrows(IllegalArgumentException.class, () ->
                compartimentoService.validarTipo(
                        compartimento,
                        TipoIngrediente.LIQUIDOS
                )
        );
        }
        // Verifica que um compartimento vazio pode trocar de tipo no dia seguinte.
        @Test
        void devePermitirTrocaDeTipoNoDiaSeguinte() {

        // Cria um compartimento para simular o cenário.
        Compartimento compartimento = new Compartimento();

        // Define o código do compartimento.
        compartimento.setCodigo("C1");

        // Informa que o último tipo armazenado foi SECOS.
        compartimento.setTipoAtual(TipoIngrediente.SECOS);

        // Simula que o estoque foi totalmente retirado.
        compartimento.setQuantidadeAtual(BigDecimal.ZERO);

        // Simula que a última movimentação ocorreu ontem.
        compartimento.setDataUltimaUtilizacao(LocalDate.now().minusDays(1));

        // Confirma que o compartimento aceita LIQUIDOS no dia seguinte.
        assertDoesNotThrow(() ->
                compartimentoService.validarTipo(
                        compartimento,
                        TipoIngrediente.LIQUIDOS
                )
        );
        }
}