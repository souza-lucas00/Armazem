package br.com.armazem.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.util.List;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura exceções do tipo RecursoNaoEncontradoException.
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception) {

        // Cria o corpo JSON da resposta de erro.
        Map<String, Object> resposta = Map.of(

                // Registra a data e hora do erro.
                "timestamp", LocalDateTime.now().toString(),

                // Informa o código HTTP do erro.
                "status", 404,

                // Informa a descrição HTTP do erro.
                "error", "Not Found",

                // Inclui a mensagem definida no Service.
                "message", exception.getMessage()
        );

        // Retorna o JSON com status HTTP 404.
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(resposta);
    }
    // Captura exceções de validação das regras de negócio.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraNegocio(
            IllegalArgumentException exception) {

        // Exibe no terminal a mensagem original da exceção.
        System.out.println(">>> HANDLER 400 EXECUTADO: "
                + exception.getMessage());

        // Monta o JSON que será enviado ao consumidor da API.
        Map<String, Object> resposta = Map.of(

                // Registra o momento em que ocorreu o erro.
                "timestamp", LocalDateTime.now().toString(),

                // Define o código HTTP da resposta.
                "status", 400,

                // Define a descrição do erro HTTP.
                "error", "Bad Request",

                // Recupera a mensagem lançada pelo Service.
                "message", exception.getMessage()
        );

        // Devolve a resposta com HTTP 400.
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resposta);
    }
    // Captura conflitos de atualização concorrente detectados pelo JPA.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> tratarConflitoConcorrencia(
            ObjectOptimisticLockingFailureException exception) {

        // Monta uma resposta padronizada para o consumidor da API.
        Map<String, Object> resposta = Map.of(

                // Registra o momento em que o conflito foi identificado.
                "timestamp", LocalDateTime.now().toString(),

                // Informa que houve um conflito de estado.
                "status", 409,

                // Define a descrição do código HTTP.
                "error", "Conflict",

                // Explica que outra operação pode ter alterado o estoque.
                "message", "O estoque foi alterado por outra operação. "
                        + "Consulte o saldo atualizado e tente novamente."
        );

        // Retorna HTTP 409 com a mensagem de conflito.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(resposta);
    }
    // Trata os erros encontrados pelo @Valid nos DTOs recebidos.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(
            MethodArgumentNotValidException exception) {

        // Percorre os campos inválidos encontrados pelo Spring.
        List<String> mensagens = exception.getBindingResult()
                .getFieldErrors()
                .stream()

                // Converte cada erro em uma mensagem compreensível.
                .map(FieldError::getDefaultMessage)

                // Reúne todas as mensagens em uma lista.
                .toList();

        // Monta a resposta padronizada da API.
        Map<String, Object> resposta = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "Bad Request",
                "messages", mensagens
        );

        // Retorna HTTP 400 com os detalhes da validação.
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resposta);
    }
}