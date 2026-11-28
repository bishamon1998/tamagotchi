package com.tamagotchi.pet;

/** SQL for the tester tools: moving a user's clock back and wiping their progress. */
final class TesterSql {

    /** Moves the pet's timestamps back, as if that much time had passed since the last visit. */
    static final String SKIP_PET_TIME = """
            UPDATE pets
            SET updated_at = updated_at - make_interval(secs => :seconds),
                asleep_until = asleep_until - make_interval(secs => :seconds)
            WHERE user_id = :userId
            """;

    /** Moves the last daily bonus back a day, so the next visit counts as the next day of the streak. */
    static final String SKIP_BONUS_DAY = """
            UPDATE wallets
            SET last_bonus_day = last_bonus_day - 1
            WHERE user_id = :userId
            """;

    /** Moves the ledger back a day, so coins earned today no longer count towards today's care cap. */
    static final String SKIP_LEDGER_DAY = """
            UPDATE coin_ledger
            SET created_at = created_at - INTERVAL '1 day'
            WHERE user_id = :userId
            """;

    /** Moves the garden's days back one, so plants grow, need water again and the free seed is given anew. */
    static final String SKIP_GARDEN_DAY = """
            UPDATE gardens
            SET free_seed_day = free_seed_day - 1
            WHERE user_id = :userId
            """;

    static final String SKIP_PLANT_DAYS = """
            UPDATE garden_plants
            SET planted_day = planted_day - 1, last_watered_day = last_watered_day - 1
            WHERE user_id = :userId
            """;

    /** Adds seeds, opening the garden first if the tester has never been there. */
    static final String ADD_SEEDS = """
            INSERT INTO gardens (user_id, common_seeds, rare_seeds, free_seed_day)
            VALUES (:userId, :common, :rare, NULL)
            ON CONFLICT (user_id) DO UPDATE
            SET common_seeds = gardens.common_seeds + :common, rare_seeds = gardens.rare_seeds + :rare
            """;

    /** Makes every plant in the tester's beds bloom now, as if watered on enough days, the last of them today. */
    static final String BLOOM_ALL = """
            UPDATE garden_plants
            SET waterings = :waterings, last_watered_day = :today
            WHERE user_id = :userId
            """;

    /** Records every flower now blooming in the tester's beds as discovered. */
    static final String DISCOVER_BLOOMING = """
            INSERT INTO discovered_flowers (user_id, flower, discovered_at)
            SELECT user_id, flower, :now FROM garden_plants WHERE user_id = :userId
            ON CONFLICT (user_id, flower) DO NOTHING
            """;

    static final String DELETE_PET = "DELETE FROM pets WHERE user_id = :userId";
    static final String DELETE_WALLET = "DELETE FROM wallets WHERE user_id = :userId";
    static final String DELETE_LEDGER = "DELETE FROM coin_ledger WHERE user_id = :userId";
    static final String DELETE_DECOR = "DELETE FROM owned_decor WHERE user_id = :userId";
    static final String DELETE_GARDEN = "DELETE FROM gardens WHERE user_id = :userId";
    static final String DELETE_PLANTS = "DELETE FROM garden_plants WHERE user_id = :userId";
    static final String DELETE_DISCOVERED = "DELETE FROM discovered_flowers WHERE user_id = :userId";
    static final String DELETE_BASKET = "DELETE FROM flower_basket WHERE user_id = :userId";

    private TesterSql() {
    }
}
