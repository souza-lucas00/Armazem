package br.com.armazem.dto;

import java.math.BigDecimal;

import br.com.armazem.enums.TipoIngrediente;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;

public record IngredienteRequest(

    @NotBlank(message = "O nome é obrigatório")
    String nome,

    @NotNull(message = "O tipo é obrigatório")
    TipoIngrediente tipo,

    // Exige que a quantidade seja informada.
    @NotNull(message = "A quantidade é obrigatória")

    // Exige uma quantidade mínima de 0.001.
    @DecimalMin(
            value = "0.001",
            message = "A quantidade deve ser maior que zero"
    )

    // Permite até 9 dígitos inteiros e 3 casas decimais.
    @Digits(
            integer = 9,
            fraction = 3,
            message = "A quantidade deve ter no máximo 3 casas decimais"
    )

    // Representa a quantidade que será armazenada.
    BigDecimal quantidade,

    @NotNull(message = "O compartimento é obrigatório")
    Long compartimentoId,

    @NotBlank(message = "O responsável é obrigatório")
    String responsavel

) {}