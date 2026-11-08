CREATE TABLE IF NOT EXISTS alunos (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    nome    TEXT    NOT NULL,
    turma   TEXT    NOT NULL,       -- "A", "B", "C"
    nota    REAL    NOT NULL,       -- 0.0 a 10.0
    status  TEXT    NOT NULL        -- "ativo" | "em_risco" | "reprovado"
);
