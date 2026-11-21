package br.com.armazem.repository;

import br.com.armazem.entity.Historico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoRepository
        extends JpaRepository<Historico, Long> {
}