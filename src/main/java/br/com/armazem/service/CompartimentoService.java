package br.com.armazem.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import br.com.armazem.entity.Compartimento;
import br.com.armazem.enums.TipoIngrediente;
// Representa listas de compartimentos.
import java.util.List;
// Permite consultar os compartimentos no banco.
import br.com.armazem.repository.CompartimentoRepository;
// Mantém uma transação ativa durante a consulta.
import org.springframework.transaction.annotation.Transactional;
@Service
public class CompartimentoService {

    public void validarCapacidade(TipoIngrediente tipo, BigDecimal quantidadeAtual, BigDecimal quantidadeEntrada) {

        BigDecimal capacidadeMaxima = tipo.getCapacidadeMaxima();

        BigDecimal novaQuantidade = quantidadeAtual.add(quantidadeEntrada);

        if (novaQuantidade.compareTo(capacidadeMaxima) > 0) {
            throw new IllegalArgumentException("Capacidade excedida para o tipo " + tipo + ". Capacidade máxima: " + capacidadeMaxima);
        }
    }
    public void validarTipo(Compartimento compartimento, TipoIngrediente novoTipo) {

        TipoIngrediente tipoAtual = compartimento.getTipoAtual();

        // Compartimento nunca utilizado
        if (tipoAtual == null) {
            return;
        }

        // Mesmo tipo: permitido
        if (tipoAtual == novoTipo) {
            return;
        }

        // Não pode misturar tipos enquanto houver estoque
        if (compartimento.getQuantidadeAtual().compareTo(BigDecimal.ZERO) > 0) {

            throw new IllegalArgumentException( "O compartimento possui ingredientes do tipo " + tipoAtual + " e não pode armazenar " + novoTipo);
        }

        // Se foi utilizado hoje, não pode trocar de tipo
        LocalDate ultimaUtilizacao = compartimento.getDataUltimaUtilizacao();

        if (ultimaUtilizacao != null && !ultimaUtilizacao.isBefore(LocalDate.now())) {

            throw new IllegalArgumentException("O compartimento só poderá armazenar outro tipo amanhã.");
        }
    }

    // Repositório responsável pelo acesso aos compartimentos no banco.
    private final CompartimentoRepository compartimentoRepository;

    // Construtor utilizado pelo Spring para injetar o repositório.
    public CompartimentoService(CompartimentoRepository compartimentoRepository) {
        // Armazena a dependência recebida para utilização nos métodos.
        this.compartimentoRepository = compartimentoRepository;
    }

    // Consulta os compartimentos que podem receber a quantidade solicitada.
    @Transactional(readOnly = true)
    public List<Compartimento> buscarDisponiveis(BigDecimal quantidade, TipoIngrediente tipo) {

        // Impede consultas com quantidade nula ou menor ou igual a zero.
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {

            // Informa que a quantidade da consulta é inválida.
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }

        // Impede consultas sem um tipo de ingrediente definido.
        if (tipo == null) {

            // Informa que o tipo é obrigatório.
            throw new IllegalArgumentException("O tipo de ingrediente é obrigatório");
        }

        // Busca todos os compartimentos cadastrados no banco.
        return compartimentoRepository.findAll().stream().filter(compartimento -> {

                // // Inicia o processamento da lista.
                // .stream()

                // // Mantém somente compartimentos que permitem esse tipo.
                // .filter(compartimento -> {

                    // Tenta validar a regra de armazenamento do tipo.
                    try {

                        // Reutiliza a validação que já desenvolvemos.
                        validarTipo(compartimento, tipo);

                        // O compartimento passou pela validação.
                        return true;

                    // Identifica um compartimento incompatível.
                    } catch (IllegalArgumentException exception) {

                        // Remove o compartimento da lista de resultados.
                        return false;
                    }
                })
                // Mantém somente os compartimentos com capacidade disponível.
                .filter(compartimento -> {

                    // Calcula quanto ficaria armazenado após a entrada.
                    BigDecimal total = compartimento.getQuantidadeAtual().add(quantidade);

                    // Verifica se o total respeita a capacidade máxima.
                    return total.compareTo(tipo.getCapacidadeMaxima()) <= 0;
                })
                // Converte os resultados em uma lista.
                .toList();
    }
    // Indica que este método realiza apenas uma consulta ao banco.
    @Transactional(readOnly = true)
    public List<Compartimento> buscarDisponiveisParaVenda(
            TipoIngrediente tipo) {

        // Verifica se o tipo foi informado.
        if (tipo == null) {

            // Impede a consulta sem um tipo válido.
            throw new IllegalArgumentException(
                    "O tipo de ingrediente é obrigatório"
            );
        }

        // Busca todos os compartimentos cadastrados.
        return compartimentoRepository.findAll()

                // Permite aplicar filtros aos resultados.
                .stream()

                // Mantém somente compartimentos do tipo solicitado.
                .filter(compartimento ->
                        compartimento.getTipoAtual() == tipo
                )

                // Mantém somente compartimentos que possuem estoque.
                .filter(compartimento ->
                        compartimento.getQuantidadeAtual()
                                .compareTo(BigDecimal.ZERO) > 0
                )

                // Converte os resultados filtrados em uma lista.
                .toList();
    }
}