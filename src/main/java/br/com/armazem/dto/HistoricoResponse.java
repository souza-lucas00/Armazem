package br.com.armazem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.armazem.entity.Historico;
import br.com.armazem.enums.TipoIngrediente;
import br.com.armazem.enums.TipoOperacao;

// Representa os dados de uma movimentação que serão retornados pela API.
public record HistoricoResponse(

        Long id,
        LocalDateTime dataHora,
        TipoOperacao operacao,
        String nomeIngrediente,
        TipoIngrediente tipo,
        BigDecimal quantidade,
        String responsavel,
        String codigoCompartimento

) {

    // Converte uma entidade Historico para o DTO de resposta.
    public static HistoricoResponse fromEntity(Historico historico) {

        // Cria o DTO utilizando os dados registrados na movimentação.
        return new HistoricoResponse(
                historico.getId(),
                historico.getDataHora(),
                historico.getOperacao(),
                historico.getNomeIngrediente(),
                historico.getTipo(),
                historico.getQuantidade(),
                historico.getResponsavel(),
                historico.getCodigoCompartimento()
        );
    }
}