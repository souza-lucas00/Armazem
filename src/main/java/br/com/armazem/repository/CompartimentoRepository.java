package br.com.armazem.repository;

import br.com.armazem.entity.Compartimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompartimentoRepository
        extends JpaRepository<Compartimento, Long> {

    Optional<Compartimento> findByCodigo(String codigo);
}