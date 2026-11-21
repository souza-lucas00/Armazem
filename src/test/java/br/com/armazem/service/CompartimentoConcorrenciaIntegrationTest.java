package br.com.armazem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.armazem.entity.Compartimento;
import br.com.armazem.repository.CompartimentoRepository;

// Inicializa o Spring Boot e utiliza o banco H2 real.
@SpringBootTest

// Cria um contexto novo antes do teste e o descarta ao terminar.
// Evita compartilhar o estado do banco com outras classes de teste.
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CompartimentoConcorrenciaIntegrationTest {

    // Repositório real utilizado para consultar e atualizar o compartimento.
    @Autowired
    private CompartimentoRepository compartimentoRepository;

    // Gerenciador responsável pelas transações do Spring.
    @Autowired
    private PlatformTransactionManager transactionManager;

   // Verifica se o controle de concorrência otimista impede
   // que uma atualização com versão antiga sobrescreva dados recentes.
   //
   // Este teste valida o comportamento do @Version no JPA.
   // Ele não simula duas requisições HTTP simultâneas nem
   // verifica o fluxo completo de cadastro e histórico.
    @Test
    void deveBloquearAtualizacaoComVersaoDesatualizada() {

        // Cria um mecanismo para executar transações independentes.
        TransactionTemplate transacao =
                new TransactionTemplate(transactionManager);

        // Primeira operação consulta o compartimento C1.
        Compartimento primeiraLeitura = transacao.execute(status ->
                compartimentoRepository.findByCodigo("C1").orElseThrow()
        );

        // Segunda operação consulta o mesmo compartimento.
        Compartimento segundaLeitura = transacao.execute(status ->
                compartimentoRepository.findByCodigo("C1").orElseThrow()
        );

        // Ambas as leituras possuem a mesma versão inicial.
        assertEquals(
                primeiraLeitura.getVersao(),
                segundaLeitura.getVersao()
        );

        // A primeira operação define um novo saldo.
        primeiraLeitura.setQuantidadeAtual(new BigDecimal("100"));

        // Salva a primeira alteração em uma transação própria.
        transacao.executeWithoutResult(status ->
                compartimentoRepository.saveAndFlush(primeiraLeitura)
        );

        // A segunda operação tenta atualizar o saldo antigo.
        segundaLeitura.setQuantidadeAtual(new BigDecimal("200"));

        // Confirma que o JPA rejeita a atualização desatualizada.
        assertThrows(OptimisticLockingFailureException.class, () ->
                transacao.executeWithoutResult(status ->
                        compartimentoRepository.saveAndFlush(segundaLeitura)
                )
        );

        // Consulta o compartimento novamente em uma nova transação.
        Compartimento atualizado = transacao.execute(status ->
                compartimentoRepository.findByCodigo("C1").orElseThrow()
        );

        // Confirma que a alteração da segunda operação não sobrescreveu a primeira.
        assertEquals(
                0,
                atualizado.getQuantidadeAtual()
                        .compareTo(new BigDecimal("100"))
        );
    }
}