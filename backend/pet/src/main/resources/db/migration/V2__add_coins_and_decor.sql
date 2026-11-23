-- Coins are counted only on the server. The balance is kept per user; every credit and debit is also logged with
-- its reason, so the balance can be audited and the daily earning cap can be checked.
CREATE TABLE wallets (
    user_id        BIGINT PRIMARY KEY,
    coins          INTEGER NOT NULL CHECK (coins >= 0),
    -- Days in a row the user came back; last_bonus_day is the game day of the last daily bonus.
    streak         INTEGER NOT NULL,
    last_bonus_day DATE
);

CREATE TABLE coin_ledger (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    -- Positive for earned coins, negative for spent ones.
    amount     INTEGER     NOT NULL,
    reason     TEXT        NOT NULL,
    -- What the coins were for: the care action or the decor item.
    detail     TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX coin_ledger_user_reason_time ON coin_ledger (user_id, reason, created_at);

-- Room decor the user bought. Each item is bought once. At most one item per slot is placed; the code keeps that
-- rule because slots belong to the catalogue, not to the data.
CREATE TABLE owned_decor (
    user_id   BIGINT      NOT NULL,
    item      TEXT        NOT NULL,
    placed    BOOLEAN     NOT NULL,
    bought_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, item)
);
