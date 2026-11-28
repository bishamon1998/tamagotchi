-- The user's garden: seeds in hand, the game day the free daily seed was last given and the flower in the vase.
CREATE TABLE gardens (
    user_id       BIGINT PRIMARY KEY,
    common_seeds  INTEGER NOT NULL CHECK (common_seeds >= 0),
    rare_seeds    INTEGER NOT NULL CHECK (rare_seeds >= 0),
    free_seed_day DATE,
    -- The flower in the vase at home, if any.
    vase_flower   TEXT
);

-- What grows in each bed. The flower is chosen when the seed is planted but stays hidden until it blooms. A plant
-- grows one stage per game day it is watered and dies after missing days, which the code works out from the days.
CREATE TABLE garden_plants (
    user_id          BIGINT  NOT NULL,
    bed              INTEGER NOT NULL,
    flower           TEXT    NOT NULL,
    seed             TEXT    NOT NULL,
    planted_day      DATE    NOT NULL,
    waterings        INTEGER NOT NULL,
    last_watered_day DATE,
    PRIMARY KEY (user_id, bed)
);

-- Flowers the user has grown to bloom at least once: the collection book.
CREATE TABLE discovered_flowers (
    user_id       BIGINT      NOT NULL,
    flower        TEXT        NOT NULL,
    discovered_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, flower)
);

-- Picked flowers waiting to be sold at the market or put in the vase at home.
CREATE TABLE flower_basket (
    user_id BIGINT  NOT NULL,
    flower  TEXT    NOT NULL,
    count   INTEGER NOT NULL CHECK (count >= 0),
    PRIMARY KEY (user_id, flower)
);
