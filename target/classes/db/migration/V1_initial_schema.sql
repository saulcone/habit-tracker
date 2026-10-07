CREATE TABLE users (
    id INT GENERATED ALWAYS AS PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    timezone VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMPZ NOT NULL DEFAULT NOW()
);

CREATE TABLE habits (
    id INT GENERATED ALWAYS AS PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_habits_user FOREIGN KEY (user_id)
                    REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_habits_user_id ON habits (user_id);

CREATE TABLE habits_logs (
    id INT GENERATED ALWAYS AS PRIMARY KEY,
    habit_id INT NOT NULL,
    completed_at TIMESTAMPZ NOT NULL DEFAULT now(),
    local_date DATE NOT NULL,
    status VARCHAR (20) NOT NULL,
    CONSTRAINT fk_habits FOREIGN KEY (habit_id)
                         REFERENCES habits(id) ON DELETE CASCADE
    CONSTRAINT uk_habit_logs_habit_date UNIQUE (habit_id, local_date)
);

CREATE INDEX idx_habit_logs_habit_date ON habit_logs (habit_id, local_date);