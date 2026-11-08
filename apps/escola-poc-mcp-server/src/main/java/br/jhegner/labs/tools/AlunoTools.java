package br.jhegner.labs.tools;

import br.jhegner.labs.model.Aluno;
import br.jhegner.labs.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.joining;

// Tools expostas ao LLM via MCP (Model Context Protocol)
// LLM nao acessa o codigo diretamente, recebe JSON schema gerado a partir das @Tool descriptions
// decide qual chamar e o Spring AI executa o metodo correspondente, retornando o resultado para o LLM
@Component
@RequiredArgsConstructor
public class AlunoTools {

    private final AlunoRepository repository;

    @Tool(description = """
            Busca informações detalhadas de um aluno pelo nome (busca parcial, case-insensitive).
            Use quando o usuário perguntar sobe um aluno específico ou quiser detalhes de um aluno.
            Retorna: nome completo, turma, nota e status (Aprovado, Reprovado ou Recuperação) de cada aluno encontrado.
            Parâmetro nome: parte do nome do aluno a ser consultado (ex: "Ana", "Silva", "Ana Silva").
            """)
    public String buscarAlunoPorNome(final String nome) {
        var alunos = repository.findByNomeContainingIgnoreCase(nome);
        if (alunos.isEmpty()) {
            return "Nenhum aluno encontrado com o nome: " + nome;
        }
        return alunos.stream()
                .map(a -> "Nome: %s | Turma: %s | Nota: %.2f | Status: %s"
                        .formatted(a.getNome(), a.getTurma(), a.getNota(), a.getStatus()))
                .collect(joining("\n"));
    }

    @Tool(description = """
            Lista todos os alunos de uma turma específica.
            Use quando usuário perguntar sobre uma turma ou quiser ver todos os alunos de uma turma.
            Parâmetro turma: nome da turma (ex: "A", "B", "C").
            Retorna: nome, nota e status de cada aluno da turma especificada.
            Se a turma não existir ou não tiver alunos, retorna uma mensagem informando.
            """)
    public String listarAlunosDaTurma(final String turma) {
        var alunos = repository.findByTurma(turma);
        if (alunos.isEmpty()) {
            return "Turma " + turma + " não encontrada ou sem alunos cadastrados.";
        }
        return ("Turma: %s (%d alunos):\n".formatted(turma.toUpperCase(), alunos.size())) +
                alunos.stream()
                        .map(a -> "- %s | Nota: %.2f | Status: %s"
                                .formatted(a.getNome(), a.getNota(), a.getStatus()))
                        .collect(joining("\n"));
    }

    @Tool(description = """
                    Lista todos os alunos com nota abaixo de um valor informado.
                    Use quando usuário perguntar sobre alunos com desempenho abaixo de uma determinada nota.
                    Algumas perguntas de exemplo: "quem está em risco", "quem tirou menos de 7", "quais alunos com nota baixa").
                    Parâmetro nota: valor da nota limite (ex: 7.0, 5.5).
                    Retorna: nome, nota e status de cada aluno com nota abaixo do limite.
                    Se nenhum aluno for encontrado, retorna uma mensagem informando.
            """)
    public String alunosAbaixoDaNota(final Double nota) {
        var alunos = repository.findByNotaLessThan(nota);
        if (alunos.isEmpty()) {
            return "Nenhum aluno encontrado com nota abaixo de " + nota;
        }
        return ("Alunos com nota abaixo de %.2f (%d alunos):\n".formatted(nota, alunos.size())) +
                alunos.stream()
                        .map(a -> "- %s (%.2f) - Turma: %s"
                                .formatted(a.getNome(), a.getNota(), a.getTurma()))
                        .collect(joining("\n"));
    }

    @Tool(description = """
                    Lista todos os alunos com nota acima de um valor informado.
                    Use quando usuário perguntar sobre alunos com desempenho acima de uma determinada nota.
                    Algumas perguntas de exemplo: "quem está bem colocado", "quem tirou mais de 7", "quais alunos com nota alta").
                    Parâmetro nota: valor da nota limite (ex: 7.0, 5.5).
                    Retorna: nome, nota e status de cada aluno com nota acima do limite.
                    Se nenhum aluno for encontrado, retorna uma mensagem informando.
            """)
    public String alunosAcimaDaNota(final Double nota) {
        var alunos = repository.findByNotaGreaterThan(nota);
        if (alunos.isEmpty()) {
            return "Nenhum aluno encontrado com nota acima de " + nota;
        }
        return ("Alunos com nota acima de %.2f (%d alunos):\n".formatted(nota, alunos.size())) +
                alunos.stream()
                        .map(a -> "- %s (%.2f) - Turma: %s"
                                .formatted(a.getNome(), a.getNota(), a.getTurma()))
                        .collect(joining("\n"));
    }

    @Tool(description = """
                    Lista todos os alunos de uma turma com nota acima de um valor e abaixo de outro.
                    Use quando usuário perguntar sobre alunos de uma turma com desempenho dentro de um intervalo.
                    Algumas perguntas de exemplo: "quem está bem colocado na turma A", "quem tirou mais de 7 e menos de 9 na turma B").
                    Parâmetros:
                    - turma: nome da turma (ex: "Turma A", "Turma B").
                    - notaMaior: valor da nota limite superior (ex: 9.0, 8.5).
                    - notaMenor: valor da nota limite inferior (ex: 7.0, 5.5).
                    Retorna: nome, nota e status de cada aluno que atende aos critérios.
                    Se nenhum aluno for encontrado, retorna uma mensagem informando.
            """)
    public String alunosDaTurmaComNotaMarioEhNotaMenor(String turma, Double notaMaior, Double notaMenor) {
        var alunos = repository.findByTurmaAndNotaGreaterThanOrNotaLessThan(turma, notaMaior, notaMenor);
        if (alunos.isEmpty()) {
            return "Nenhum aluno encontrado com nota acima de " + notaMaior;
        }
        return ("Alunos da turma %s com nota entre %.2f e %.2f (%d alunos):\n"
                .formatted(turma, notaMenor, notaMaior, alunos.size())) +
                alunos.stream()
                        .map(a -> "- %s (%.2f) - Status: %s"
                                .formatted(a.getNome(), a.getNota(), a.getStatus()))
                        .collect(joining("\n"));
    }

    @Tool(description = """
                    Lista todos os alunos com um status específico (Aprovado, Reprovado ou Recuperação).
                    Use quando usuário perguntar sobre alunos com determinado status.
                    Algumas perguntas de exemplo: "quem está aprovado", "quais alunos estão em recuperação").
                    Parâmetro status: status do aluno (ex: "Aprovado", "Reprovado", "Recuperação").
                    Retorna: nome, nota e turma de cada aluno com o status informado.
                    Se nenhum aluno for encontrado, retorna uma mensagem informando.
            """)
    public String alunosPorStatus(String status) {
        var alunos = repository.findByStatus(status);
        if (alunos.isEmpty()) {
            return "Nenhum aluno encontrado com status " + status;
        }
        return ("Alunos com status %s (%d alunos):\n".formatted(status, alunos.size())) +
                alunos.stream()
                        .map(a -> "- %s (%.2f) - Turma: %s"
                                .formatted(a.getNome(), a.getNota(), a.getTurma()))
                        .collect(joining("\n"));
    }

    @Tool(description = """
            Retorna resumo estatístico geral da escola.
            Use para perguntas gerais como: "como está a escola?",
            "me dê um panorama", "quantos alunos temos?".
            Retorna: total de alunos, média geral, contagem por status.
            Não requer parâmetros.
            """)
    public String resumoGeral() {

        var todos = repository.findAll();

        if (todos.isEmpty()) {
            return "Nenhum aluno cadastrado.";
        }

        double media = todos.stream()
                .mapToDouble(Aluno::getNota).average().orElse(0.0);

        long aprovados = todos.stream().filter(a -> "Aprovado".equals(a.getStatus())).count();
        long recuperados = todos.stream().filter(a -> "Recuperacao".equals(a.getStatus())).count();
        long reprovados = todos.stream().filter(a -> "Reprovado".equals(a.getStatus())).count();

        return """
                Resumo da Escola Beta:
                - Total de alunos: %d
                - Média geral das notas: %.2f
                - Alunos aprovados: %d
                - Alunos em recuperação: %d
                - Alunos reprovados: %d
                """.formatted(todos.size(), media, aprovados, recuperados, reprovados);
    }

}
