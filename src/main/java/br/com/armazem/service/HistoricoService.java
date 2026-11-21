package br.com.armazem.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.armazem.dto.HistoricoResponse;
import br.com.armazem.entity.Historico;
import br.com.armazem.repository.HistoricoRepository;

@Service
public class HistoricoService {

    // Repositório responsável pelo acesso aos registros de histórico.
    private final HistoricoRepository historicoRepository;

    // Recebe o repositório por injeção de dependência.
    public HistoricoService(HistoricoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    // Executa a consulta sem realizar alterações no banco.
    @Transactional(readOnly = true)
    public List<HistoricoResponse> listar(String sortBy, String order) {

        // Define o critério de ordenação solicitado.
        Comparator<Historico> comparador;

        // Seleciona o campo utilizado para ordenar os registros.
        if ("date".equalsIgnoreCase(sortBy)) {

            // Ordena primeiro pela data e depois pelo código do compartimento.
            comparador = Comparator
                    .comparing(Historico::getDataHora)
                    .thenComparing(Historico::getCodigoCompartimento);

        } else if ("compartimento".equalsIgnoreCase(sortBy)) {

            // Ordena primeiro pelo compartimento e depois pela data.
            comparador = Comparator
                    .comparing(Historico::getCodigoCompartimento)
                    .thenComparing(Historico::getDataHora);

        } else {

            // Rejeita campos de ordenação que não são permitidos.
            throw new IllegalArgumentException(
                    "Ordenação inválida. Utilize date ou compartimento."
            );
        }

        // Inverte a ordem caso o usuário solicite DESC.
        if ("desc".equalsIgnoreCase(order)) {

            // Aplica a ordenação decrescente.
            comparador = comparador.reversed();

        } else if (!"asc".equalsIgnoreCase(order)) {

            // Impede valores diferentes de ASC ou DESC.
            throw new IllegalArgumentException(
                    "Ordem inválida. Utilize asc ou desc."
            );
        }

        // Consulta os registros e aplica a ordenação selecionada.
        return historicoRepository.findAll()
                .stream()
                .sorted(comparador)

                // Converte cada registro para o DTO de resposta.
                .map(HistoricoResponse::fromEntity)

                // Reúne os resultados em uma lista.
                .toList();
    }
}