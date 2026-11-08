package br.jhegner.labs.repository;

import br.jhegner.labs.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  AlunoRepository extends JpaRepository<Aluno, Long> {

    // SELECT * FROM alunos WHERE LOWER(nome) LIKE LOWER('%nome%')
    List<Aluno> findByNomeContainingIgnoreCase(String nome);

    // SELECT * FROM alunos WHERE turma = ?
    List<Aluno> findByTurma(String turma);

    // SELECT * FROM alunos WHERE nota < ?
    List<Aluno> findByNotaLessThan(Double nota);

    // SELECT * FROM alunos WHERE nota > ?
    List<Aluno> findByNotaGreaterThan(Double nota);

    // SELECT * FROM alunos WHERE turma and nota > ? or nota < ?
    List<Aluno> findByTurmaAndNotaGreaterThanOrNotaLessThan(String turma, Double notaMaior, Double notaMenor);

    // SELECT * FROM alunos WHERE status = ?
    List<Aluno> findByStatus(String status);
}
