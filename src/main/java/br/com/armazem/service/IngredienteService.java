package br.com.armazem.service;
import java.util.EnumMap;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.armazem.dto.IngredienteRequest;
import br.com.armazem.dto.IngredienteResponse;
import br.com.armazem.dto.SaidaIngredienteRequest;
import br.com.armazem.entity.Compartimento;
import br.com.armazem.entity.Historico;
import br.com.armazem.entity.Ingrediente;
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.enums.TipoOperacao;
import br.com.armazem.repository.CompartimentoRepository;
import br.com.armazem.repository.HistoricoRepository;
import br.com.armazem.repository.IngredienteRepository;

// Permite criar um mapa que associa cada tipo ao seu volume.
import java.util.Map;

// Importa nossa exceção personalizada.
import br.com.armazem.exception.RecursoNaoEncontradoException;

@Service
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final CompartimentoRepository compartimentoRepository;
    private final HistoricoRepository historicoRepository;
    private final CompartimentoService compartimentoService;

    public IngredienteService(
            IngredienteRepository ingredienteRepository,
            CompartimentoRepository compartimentoRepository,
            HistoricoRepository historicoRepository,
            CompartimentoService compartimentoService) {

        this.ingredienteRepository = ingredienteRepository;
        this.compartimentoRepository = compartimentoRepository;
        this.historicoRepository = historicoRepository;
        this.compartimentoService = compartimentoService;
    }

    @Transactional
    public Ingrediente cadastrar(IngredienteRequest request) {

        // 1. Busca o compartimento pelo identificador informado no cadastro.
        Compartimento compartimento = compartimentoRepository
                .findById(request.compartimentoId())

                // Lança uma exceção específica quando o compartimento não existe.
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Compartimento não encontrado: " + request.compartimentoId()
                ));
        // Mostra no terminal que a validação será iniciada.
        System.out.println(">>> INICIANDO VALIDACAO DE TIPO");
        // 2. Validar as regras de armazenamento
        compartimentoService.validarTipo(
                compartimento,
                request.tipo()
        );

        compartimentoService.validarCapacidade(
                request.tipo(),
                compartimento.getQuantidadeAtual(),
                request.quantidade()
        );
// Esta mensagem só aparece se a validação não lançar exceção.
System.out.println(">>> VALIDACAO DE TIPO CONCLUIDA");
        // 3. Criar e salvar o ingrediente
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setNome(request.nome());
        ingrediente.setTipo(request.tipo());
        ingrediente.setQuantidade(request.quantidade());
        ingrediente.setCompartimento(compartimento);

        ingrediente = ingredienteRepository.save(ingrediente);

        // 4. Atualizar o compartimento
        compartimento.setTipoAtual(request.tipo());

        compartimento.setQuantidadeAtual(
                compartimento.getQuantidadeAtual()
                        .add(request.quantidade())
        );

        compartimento.setDataUltimaUtilizacao(LocalDate.now());

        compartimentoRepository.save(compartimento);

        // 5. Registrar o histórico
        Historico historico = new Historico();

        historico.setDataHora(LocalDateTime.now());
        historico.setOperacao(TipoOperacao.ENTRADA);
        historico.setQuantidade(request.quantidade());
        historico.setTipo(request.tipo());
        historico.setResponsavel(request.responsavel());
        historico.setNomeIngrediente(request.nome());
        historico.setCodigoCompartimento(compartimento.getCodigo());
        historico.setIngrediente(ingrediente);
        historico.setCompartimento(compartimento);

        historicoRepository.save(historico);

        return ingrediente;
    }
    @Transactional(readOnly = true)
    public List<IngredienteResponse> listarTodos() {

        return ingredienteRepository.findAll()
                .stream()
                .map(IngredienteResponse::fromEntity)
                .toList();
    }
    // Define uma transação de somente leitura para consultar os dados.
    // Inicia uma transação de somente leitura.
    @Transactional(readOnly = true)
    public IngredienteResponse buscarPorId(Long id) {

        // Procura um ingrediente pelo ID informado.
        Ingrediente ingrediente = ingredienteRepository.findById(id)

                // Se não encontrar, lança uma exceção específica.
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ingrediente não encontrado: " + id
                ));

        // Converte a entidade para um DTO antes de devolvê-la.
        return IngredienteResponse.fromEntity(ingrediente);
    }
    // Indica que o método consulta dados sem realizar alterações.
    @Transactional(readOnly = true)
    public Map<TipoIngrediente, BigDecimal> consultarVolumePorTipo() {

        // Cria um mapa em que cada chave é um TipoIngrediente
        // e cada valor representa o total armazenado daquele tipo.
        Map<TipoIngrediente, BigDecimal> volumes =
                new EnumMap<>(TipoIngrediente.class);

        // Inicializa todos os tipos com volume zero.
        for (TipoIngrediente tipo : TipoIngrediente.values()) {

            // Registra zero como volume inicial do tipo.
            volumes.put(tipo, BigDecimal.ZERO);
        }

        // Percorre todos os ingredientes cadastrados no banco.
        for (Ingrediente ingrediente : ingredienteRepository.findAll()) {

            // Identifica o tipo do ingrediente atual.
            TipoIngrediente tipo = ingrediente.getTipo();

            // Recupera o volume já acumulado para esse tipo.
            BigDecimal volumeAtual = volumes.get(tipo);

            // Soma a quantidade do ingrediente ao volume acumulado.
            BigDecimal novoVolume = volumeAtual.add(ingrediente.getQuantidade());

            // Atualiza o total armazenado daquele tipo.
            volumes.put(tipo, novoVolume);
        }

        // Devolve o mapa com os volumes totais por tipo.
        return volumes;
    }

    // Garante que a atualização do estoque e o histórico
    // sejam confirmados ou desfeitos juntos.
    @Transactional
    public IngredienteResponse registrarSaida(
            Long id,
            SaidaIngredienteRequest request) {

        // Busca o ingrediente que terá sua quantidade reduzida.
        Ingrediente ingrediente = ingredienteRepository.findById(id)

                // Retorna erro 404 caso o ingrediente não exista.
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ingrediente não encontrado: " + id
                ));

        // Obtém a quantidade solicitada para retirada.
        BigDecimal quantidadeSaida = request.quantidade();

        // Valida se a quantidade foi informada e é positiva.
        if (quantidadeSaida == null
                || quantidadeSaida.compareTo(BigDecimal.ZERO) <= 0) {

            // Impede uma retirada com quantidade inválida.
            throw new IllegalArgumentException(
                    "A quantidade de saída deve ser maior que zero"
            );
        }

        // Verifica se existe estoque suficiente no ingrediente.
        if (ingrediente.getQuantidade().compareTo(quantidadeSaida) < 0) {

            // Impede que o estoque fique negativo.
            throw new IllegalArgumentException(
                    "Estoque insuficiente para realizar a saída"
            );
        }

        // Obtém o compartimento onde o ingrediente está armazenado.
        Compartimento compartimento = ingrediente.getCompartimento();

        // Calcula a nova quantidade do ingrediente.
        BigDecimal novaQuantidadeIngrediente =
                ingrediente.getQuantidade().subtract(quantidadeSaida);

        // Calcula o novo total armazenado no compartimento.
        BigDecimal novaQuantidadeCompartimento =
                compartimento.getQuantidadeAtual().subtract(quantidadeSaida);

        // Atualiza o saldo do ingrediente.
        ingrediente.setQuantidade(novaQuantidadeIngrediente);

        // Atualiza o estoque total do compartimento.
        compartimento.setQuantidadeAtual(novaQuantidadeCompartimento);

        // Registra a data do último uso do compartimento.
        compartimento.setDataUltimaUtilizacao(LocalDate.now());

        // Mantém o tipoAtual mesmo quando o estoque chegar a zero.
        // Isso impede a troca de tipo no mesmo dia.

        // Persiste a alteração do ingrediente.
        ingredienteRepository.save(ingrediente);

        // Persiste a alteração do compartimento.
        compartimentoRepository.save(compartimento);

        // Cria um registro para documentar a movimentação.
        Historico historico = new Historico();

        // Registra o momento da saída.
        historico.setDataHora(LocalDateTime.now());

        // Identifica a movimentação como SAIDA.
        historico.setOperacao(TipoOperacao.SAIDA);

        // Registra a quantidade retirada.
        historico.setQuantidade(quantidadeSaida);

        // Registra o tipo do ingrediente movimentado.
        historico.setTipo(ingrediente.getTipo());

        // Identifica quem realizou a retirada.
        historico.setResponsavel(request.responsavel());

        // Preserva o nome do ingrediente no histórico.
        historico.setNomeIngrediente(ingrediente.getNome());

        // Preserva o código do compartimento no histórico.
        historico.setCodigoCompartimento(compartimento.getCodigo());

        // Associa o histórico ao ingrediente.
        historico.setIngrediente(ingrediente);

        // Associa o histórico ao compartimento.
        historico.setCompartimento(compartimento);

        // Salva o histórico na mesma transação.
        historicoRepository.save(historico);

        // Converte o ingrediente atualizado para o DTO de resposta.
        return IngredienteResponse.fromEntity(ingrediente);
    }
}
