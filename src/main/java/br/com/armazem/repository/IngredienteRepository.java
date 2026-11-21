package br.com.armazem.repository;

import br.com.armazem.entity.Ingrediente;
import br.com.armazem.enums.TipoIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredienteRepository
        extends JpaRepository<Ingrediente, Long> {

    List<Ingrediente> findByTipo(TipoIngrediente tipo);
}