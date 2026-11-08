
# db

sqlite3 --version

sudo apt update && sudo apt install sqlite3

cd apps/escola-poc-mcp-server
sqlite3 escola.db

SELECT COUNT(*) FROM alunos;
SELECT * FROM alunos;