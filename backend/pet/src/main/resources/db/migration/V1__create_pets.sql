-- One pet per Telegram user. Needs are stored as of updated_at; the drop since then is computed on read.
CREATE TABLE pets (
    user_id    BIGINT PRIMARY KEY,
    satiety    DOUBLE PRECISION NOT NULL,
    mood       DOUBLE PRECISION NOT NULL,
    energy     DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMPTZ      NOT NULL
);
