package br.jhegner.labs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "alunos")
@NoArgsConstructor
@AllArgsConstructor
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String turma;

    @Column(nullable = false)
    private Double nota;

    @Column(nullable = false)
    private String status; // "Aprovado" ou "Reprovado" ou "Recuperação"

    @Override
    public String toString() {
        return "Aluno{id=%d, nome='%s', turma='%s', nota=%.2f, status='%s'}"
                .formatted(id, nome, turma, nota, status);
    }
}
