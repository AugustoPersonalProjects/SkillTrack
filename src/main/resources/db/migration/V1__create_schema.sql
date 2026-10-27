CREATE TABLE st_categories (
    id_category BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT st_categories_pk PRIMARY KEY (id_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE st_usuarios (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    data_criacao DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT st_usuarios_pk PRIMARY KEY (id_usuario),
    CONSTRAINT st_usuarios_email_uq UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE st_skills (
    id_skill BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    skill_level VARCHAR(20) NOT NULL,
    category_id BIGINT,
    user_id BIGINT,
    CONSTRAINT st_skills_pk PRIMARY KEY (id_skill),
    CONSTRAINT st_skills_category_fk FOREIGN KEY (category_id)
        REFERENCES st_categories (id_category),
    CONSTRAINT st_skills_user_fk FOREIGN KEY (user_id)
        REFERENCES st_usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE st_goals (
    id_goal BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(255) NOT NULL,
    deadline DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    skill_id BIGINT,
    user_id BIGINT,
    CONSTRAINT st_goals_pk PRIMARY KEY (id_goal),
    CONSTRAINT st_goals_skill_fk FOREIGN KEY (skill_id)
        REFERENCES st_skills (id_skill) ON DELETE CASCADE,
    CONSTRAINT st_goals_user_fk FOREIGN KEY (user_id)
        REFERENCES st_usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE st_progress (
    id_progress BIGINT NOT NULL AUTO_INCREMENT,
    percentage INT NOT NULL,
    date_progress DATE,
    notes VARCHAR(255),
    skill_id BIGINT,
    goal_id BIGINT,
    CONSTRAINT st_progress_pk PRIMARY KEY (id_progress),
    CONSTRAINT st_progress_percentage_ck CHECK (percentage BETWEEN 0 AND 100),
    CONSTRAINT st_progress_skill_fk FOREIGN KEY (skill_id)
        REFERENCES st_skills (id_skill) ON DELETE CASCADE,
    CONSTRAINT st_progress_goal_fk FOREIGN KEY (goal_id)
        REFERENCES st_goals (id_goal) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
