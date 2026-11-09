-- Dados de exemplo - executado apo schema.sql

-- Limpa antes de inserir
DELETE FROM alunos;

-- Turma A
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Ana Silva',       'A', 8.5, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Bruno Costa',     'A', 4.2, 'Recuperacao');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Carlos Souza',    'A', 7.8, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Diana Lima',      'A', 9.3, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Eduardo Matos',   'A', 3.1, 'Reprovado');

-- Turma B
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Fernanda Rocha',  'B', 6.4, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Gabriel Nunes',   'B', 2.8, 'Reprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Helena Castro',   'B', 9.7, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Igor Ferreira',   'B', 4.9, 'Recuperacao');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Julia Cardoso',   'B', 7.1, 'Aprovado');

-- Turma C
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Kevin Alves',     'C', 5.5, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Laura Martins',   'C', 8.9, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Marcos Pereira',  'C', 1.5, 'Reprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Natalia Gomes',   'C', 6.0, 'Aprovado');
INSERT INTO alunos (nome, turma, nota, status) VALUES ('Otavio Ribeiro',  'C', 4.5, 'Recuperacao');