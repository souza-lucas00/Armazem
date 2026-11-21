// Define o pacote ao qual esta classe pertence.
package br.com.armazem.exception;

// Cria uma exceção específica para recursos não encontrados.
public class RecursoNaoEncontradoException extends RuntimeException {

    // Construtor que recebe a mensagem explicando o erro.
    public RecursoNaoEncontradoException(String mensagem) {

        // Envia a mensagem para a classe RuntimeException.
        super(mensagem);
    }
}