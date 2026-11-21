package br.com.armazem.enums;

import java.math.BigDecimal;

public enum TipoIngrediente {
   SECOS(new BigDecimal("600")),
   LIQUIDOS(new BigDecimal("500")),
   REFRIGERADOS(new BigDecimal("400"));

    private final BigDecimal capacidadeMaxima;

   TipoIngrediente(BigDecimal capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
    }

    public BigDecimal getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
