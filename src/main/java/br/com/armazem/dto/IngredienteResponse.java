package br.com.armazem.dto;

import java.math.BigDecimal;

import br.com.armazem.entity.Ingrediente;
import br.com.armazem.enums.TipoIngrediente;

public record IngredienteResponse(
        Long id,
        String nome,
        TipoIngrediente tipo,
        BigDecimal quantidade,
        Long compartimentoId,
        String codigoCompartimento
) {

    public static IngredienteResponse fromEntity(Ingrediente ingrediente) {
        return new IngredienteResponse(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getTipo(),
                ingrediente.getQuantidade(),
                ingrediente.getCompartimento().getId(),
                ingrediente.getCompartimento().getCodigo()
        );
    }
}