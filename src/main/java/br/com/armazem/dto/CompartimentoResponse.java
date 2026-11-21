// Define o pacote do DTO.
package br.com.armazem.dto;

// Permite representar quantidades decimais.
import java.math.BigDecimal;

// Importa a entidade Compartimento.
import br.com.armazem.entity.Compartimento;

// Importa o enum de tipos de ingrediente.
import br.com.armazem.enums.TipoIngrediente;

// Define os dados que serão enviados ao consumidor da API.
// Identificador do compartimento.
// Código do compartimento, como C1 ou C2.
// Tipo atualmente associado ao compartimento.
// Quantidade armazenada atualmente.
// Espaço que ainda pode ser utilizado para o tipo consultado.
public record CompartimentoResponse(Long id, String codigo, TipoIngrediente tipoAtual, BigDecimal quantidadeAtual, BigDecimal capacidadeDisponivel) {

    // Converte a entidade para o DTO de resposta.
    public static CompartimentoResponse fromEntity(Compartimento compartimento, TipoIngrediente tipoConsultado) {

        // Calcula o espaço restante para o tipo solicitado.
        BigDecimal capacidadeDisponivel = tipoConsultado.getCapacidadeMaxima().subtract(compartimento.getQuantidadeAtual());

        // Monta e retorna o DTO.
        return new CompartimentoResponse(compartimento.getId(), compartimento.getCodigo(), compartimento.getTipoAtual(), compartimento.getQuantidadeAtual(), capacidadeDisponivel);
    }
}