package br.edu.utfpr.td.tsi.repository;

import br.edu.utfpr.td.tsi.model.Medico;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends MongoRepository<Medico, String> {

    Optional<Medico> findByCrmAndUf(String crm, String uf);

    List<Medico> findByNomeContainingIgnoreCase(String nome);

    List<Medico> findByCrm(String crm);

    List<Medico> findByEspecialidadeContainingIgnoreCase(String especialidade);
}