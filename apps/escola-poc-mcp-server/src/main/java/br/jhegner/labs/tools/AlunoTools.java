package br.jhegner.labs.tools;

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
    public String buscarAlunoPorNome(final String nome){
        var alunos = repository.findByNomeContainingIgnoreCase(nome);
        if (alunos.isEmpty()) {
            return "Nenhum aluno encontrado com o nome: " + nome;
        }
        return alunos.stream()
                .map(a -> "Nome: %s | Turma: %s | Nota: %.2f | Status: %s"
                        .formatted(a.getNome(), a.getTurma(), a.getNota(), a.getStatus()))
                .collect(joining("\n"));
    }
}
