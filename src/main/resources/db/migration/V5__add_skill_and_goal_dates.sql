-- Sem valor padrão: os registros antigos ficam sem data, porque o dia em que foram criados não é conhecido
ALTER TABLE st_skills
    ADD COLUMN data_criacao DATETIME(6);

ALTER TABLE st_goals
    ADD COLUMN data_criacao DATETIME(6),
    ADD COLUMN data_conclusao DATETIME(6);
