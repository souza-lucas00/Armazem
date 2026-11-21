// Define o pacote onde esta classe está localizada.
package br.com.armazem.dto;

// Importa o tipo usado para representar quantidades decimais.
import java.math.BigDecimal;

// Importa a validação de valor mínimo.
import jakarta.validation.constraints.DecimalMin;

// Importa a validação de texto obrigatório.
import jakarta.validation.constraints.NotBlank;

// Importa a validação de campo não nulo.
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;

// Define o DTO que receberá os dados de saída do estoque.
public record SaidaIngredienteRequest(

        // Impede que a quantidade seja nula.
        @NotNull(message = "A quantidade é obrigatória")

        // Exige uma quantidade mínima de 0.001.
        @DecimalMin(
                value = "0.001",
                message = "A quantidade deve ser maior que zero"
        )

        // Limita a quantidade a três casas decimais.
        @Digits(
                integer = 9,
                fraction = 3,
                message = "A quantidade deve ter no máximo 3 casas decimais"
        )

        // Quantidade que será retirada do estoque.
        BigDecimal quantidade,

        // Exige que o responsável seja informado.
        @NotBlank(message = "O responsável é obrigatório")

        // Representa o nome de quem realizou a saída.
        String responsavel

) {}