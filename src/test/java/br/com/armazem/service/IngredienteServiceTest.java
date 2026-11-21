package br.com.armazem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.armazem.dto.IngredienteRequest;
import br.com.armazem.entity.Compartimento;
import br.com.armazem.entity.Historico;
import br.com.armazem.entity.Ingrediente;
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.enums.TipoOperacao;
import br.com.armazem.repository.CompartimentoRepository;
import br.com.armazem.repository.HistoricoRepository;
import br.com.armazem.repository.IngredienteRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;

import br.com.armazem.dto.SaidaIngredienteRequest;

// Habilita o Mockito para inicializar os objetos simulados.
@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    // Simula o repositório de ingredientes.
    @Mock
    private IngredienteRepository ingredienteRepository;

    // Simula o repositório de compartimentos.
    @Mock
    private CompartimentoRepository compartimentoRepository;

    // Simula o repositório de histórico.
    @Mock
    private HistoricoRepository historicoRepository;

    // Serviço responsável pelas validações de armazenamento.
    private CompartimentoService compartimentoService;

    // Serviço que será testado.
    private IngredienteService ingredienteService;

    // Executa a configuração antes de cada teste.
    @BeforeEach
    void configurar() {

        // Cria o serviço de compartimentos com o repositório simulado.
        compartimentoService = new CompartimentoService(
                compartimentoRepository
        );

        // Cria o serviço de ingredientes com todas as dependências.
        ingredienteService = new IngredienteService(
                ingredienteRepository,
                compartimentoRepository,
                historicoRepository,
                compartimentoService
        );
    }

    // Verifica que cadastrar um ingrediente atualiza o estoque e gera histórico.
    @Test
    void deveCadastrarIngredienteERegistrarEntradaNoHistorico() {

        // Cria um compartimento inicialmente vazio.
        Compartimento compartimento = new Compartimento();

        // Define o ID do compartimento.
        compartimento.setId(1L);

        // Define seu código.
        compartimento.setCodigo("C1");

        // Define a quantidade inicial como zero.
        compartimento.setQuantidadeAtual(BigDecimal.ZERO);

        // Simula a busca do compartimento pelo ID.
        when(compartimentoRepository.findById(1L))
                .thenReturn(Optional.of(compartimento));

        // Simula o salvamento do ingrediente, devolvendo o próprio objeto.
        when(ingredienteRepository.save(any(Ingrediente.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        // Monta a requisição de cadastro.
        IngredienteRequest request = new IngredienteRequest(
                "Arroz",
                TipoIngrediente.SECOS,
                new BigDecimal("100"),
                1L,
                "João"
        );

        // Executa o cadastro utilizando o serviço real.
        Ingrediente ingrediente = ingredienteService.cadastrar(request);

        // Verifica se o ingrediente foi criado.
        assertNotNull(ingrediente);

        // Confirma que o ingrediente possui 100 kg.
        assertEquals(
                0,
                ingrediente.getQuantidade().compareTo(new BigDecimal("100"))
        );

        // Confirma que o compartimento passou a armazenar 100 kg.
        assertEquals(
                0,
                compartimento.getQuantidadeAtual()
                        .compareTo(new BigDecimal("100"))
        );

        // Confirma que o compartimento passou a armazenar SECOS.
        assertEquals(
                TipoIngrediente.SECOS,
                compartimento.getTipoAtual()
        );

        // Captura o objeto de histórico enviado para o repositório.
        ArgumentCaptor<Historico> captor =
                ArgumentCaptor.forClass(Historico.class);

        // Verifica que o método save do histórico foi chamado.
        verify(historicoRepository).save(captor.capture());

        // Recupera o registro capturado.
        Historico historico = captor.getValue();

        // Confirma que a operação registrada foi uma ENTRADA.
        assertEquals(TipoOperacao.ENTRADA, historico.getOperacao());

        // Confirma a quantidade registrada no histórico.
        assertEquals(
                0,
                historico.getQuantidade().compareTo(new BigDecimal("100"))
        );

        // Confirma quem realizou a movimentação.
        assertEquals("João", historico.getResponsavel());

        // Confirma o compartimento registrado.
        assertEquals("C1", historico.getCodigoCompartimento());
    }
    
    // Verifica se a saída reduz o estoque e registra a movimentação.
        @Test
        void deveRegistrarSaidaEAtualizarEstoque() {

        // Cria o compartimento onde o arroz está armazenado.
        Compartimento compartimento = new Compartimento();

        // Define seu identificador.
        compartimento.setId(1L);

        // Define o código do compartimento.
        compartimento.setCodigo("C1");

        // Informa o tipo armazenado.
        compartimento.setTipoAtual(TipoIngrediente.SECOS);

        // Define que C1 possui 100 kg.
        compartimento.setQuantidadeAtual(new BigDecimal("100"));

        // Cria o ingrediente que será utilizado no teste.
        Ingrediente ingrediente = new Ingrediente();

        // Define seu identificador.
        ingrediente.setId(1L);

        // Define o nome do ingrediente.
        ingrediente.setNome("Arroz");

        // Define o tipo do ingrediente.
        ingrediente.setTipo(TipoIngrediente.SECOS);

        // Define o estoque inicial do arroz.
        ingrediente.setQuantidade(new BigDecimal("100"));

        // Associa o arroz ao compartimento C1.
        ingrediente.setCompartimento(compartimento);

        // Simula a consulta do ingrediente no banco.
        when(ingredienteRepository.findById(1L))
                .thenReturn(Optional.of(ingrediente));

        // Cria a requisição para retirar 30 kg de arroz.
        SaidaIngredienteRequest request = new SaidaIngredienteRequest(
                new BigDecimal("30"),
                "João"
        );

        // Executa a operação de saída.
        ingredienteService.registrarSaida(1L, request);

        // Confirma que o saldo do ingrediente passou para 70 kg.
        assertEquals(
                0,
                ingrediente.getQuantidade().compareTo(new BigDecimal("70"))
        );

        // Confirma que o compartimento também passou para 70 kg.
        assertEquals(
                0,
                compartimento.getQuantidadeAtual()
                        .compareTo(new BigDecimal("70"))
        );

        // Captura o histórico enviado ao repositório.
        ArgumentCaptor<Historico> captor =
                ArgumentCaptor.forClass(Historico.class);

        // Confirma que o histórico foi enviado para salvamento.
        verify(historicoRepository).save(captor.capture());

        // Obtém o registro de histórico capturado.
        Historico historico = captor.getValue();

        // Confirma que a operação registrada foi SAIDA.
        assertEquals(TipoOperacao.SAIDA, historico.getOperacao());

        // Confirma que a quantidade registrada foi 30 kg.
        assertEquals(
                0,
                historico.getQuantidade().compareTo(new BigDecimal("30"))
        );

        // Confirma quem realizou a retirada.
        assertEquals("João", historico.getResponsavel());

        // Confirma o compartimento de origem.
        assertEquals("C1", historico.getCodigoCompartimento());

        // Confirma que o tipo do compartimento permanece SECOS.
        assertEquals(
                TipoIngrediente.SECOS,
                compartimento.getTipoAtual()
        );
        }
        // Verifica que não é possível retirar mais do que o estoque disponível.
        @Test
        void deveBloquearSaidaComEstoqueInsuficiente() {

        // Cria o compartimento utilizado no teste.
        Compartimento compartimento = new Compartimento();

        // Define seu identificador.
        compartimento.setId(1L);

        // Define o código.
        compartimento.setCodigo("C1");

        // Define o tipo armazenado.
        compartimento.setTipoAtual(TipoIngrediente.SECOS);

        // Define o estoque total do compartimento.
        compartimento.setQuantidadeAtual(new BigDecimal("100"));

        // Cria o ingrediente que será consultado.
        Ingrediente ingrediente = new Ingrediente();

        // Define seu identificador.
        ingrediente.setId(1L);

        // Define o nome.
        ingrediente.setNome("Arroz");

        // Define o tipo.
        ingrediente.setTipo(TipoIngrediente.SECOS);

        // Define que existem 100 kg de arroz.
        ingrediente.setQuantidade(new BigDecimal("100"));

        // Associa o ingrediente ao compartimento.
        ingrediente.setCompartimento(compartimento);

        // Simula a consulta do ingrediente pelo ID.
        when(ingredienteRepository.findById(1L))
                .thenReturn(Optional.of(ingrediente));

        // Cria uma solicitação para retirar 150 kg.
        SaidaIngredienteRequest request = new SaidaIngredienteRequest(
                new BigDecimal("150"),
                "João"
        );

        // Confirma que a saída é rejeitada por falta de estoque.
        assertThrows(IllegalArgumentException.class, () ->
                ingredienteService.registrarSaida(1L, request)
        );

        // Confirma que o saldo do ingrediente não foi alterado.
        assertEquals(
                0,
                ingrediente.getQuantidade().compareTo(new BigDecimal("100"))
        );

        // Confirma que o estoque do compartimento permanece inalterado.
        assertEquals(
                0,
                compartimento.getQuantidadeAtual()
                        .compareTo(new BigDecimal("100"))
        );

        // Confirma que o ingrediente não foi enviado para salvamento.
        verify(ingredienteRepository, never()).save(any(Ingrediente.class));

        // Confirma que o compartimento não foi enviado para salvamento.
        verify(compartimentoRepository, never()).save(any(Compartimento.class));

        // Confirma que nenhuma movimentação inválida gerou histórico.
        verify(historicoRepository, never()).save(any(Historico.class));
        }
}