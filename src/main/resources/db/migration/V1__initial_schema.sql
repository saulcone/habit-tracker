CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       timezone VARCHAR(50) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE habits (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        description TEXT,
                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        CONSTRAINT fk_habits_user FOREIGN KEY (user_id)
                            REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_habits_user_id ON habits (user_id);

CREATE TABLE habit_logs (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            habit_id BIGINT NOT NULL,
                            completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            local_date DATE NOT NULL,
                            status VARCHAR(20) NOT NULL,
                            CONSTRAINT fk_habit_logs_habit FOREIGN KEY (habit_id)
                                REFERENCES habits (id) ON DELETE CASCADE,
                            CONSTRAINT uk_habit_logs_habit_date UNIQUE (habit_id, local_date)
);
CREATE INDEX idx_habit_logs_habit_date ON habit_logs (habit_id, local_date);