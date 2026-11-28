package com.tamagotchi.pet;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Rewrites a tester's own rows so they can check what happens later without waiting. */
@Repository
class TesterRepository {

    private final JdbcClient jdbc;

    TesterRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    void skipPetTime(long userId, Duration skip) {
        jdbc.sql(TesterSql.SKIP_PET_TIME)
                .param("userId", userId)
                .param("seconds", skip.toSeconds())
                .update();
    }

    void skipDay(long userId) {
        run(List.of(TesterSql.SKIP_BONUS_DAY, TesterSql.SKIP_LEDGER_DAY, TesterSql.SKIP_GARDEN_DAY,
                TesterSql.SKIP_PLANT_DAYS), userId);
    }

    void deleteProgress(long userId) {
        run(List.of(TesterSql.DELETE_PET, TesterSql.DELETE_WALLET, TesterSql.DELETE_LEDGER, TesterSql.DELETE_DECOR,
                TesterSql.DELETE_GARDEN, TesterSql.DELETE_PLANTS, TesterSql.DELETE_DISCOVERED, TesterSql.DELETE_BASKET),
                userId);
    }

    void addSeeds(long userId, int common, int rare) {
        jdbc.sql(TesterSql.ADD_SEEDS).param("userId", userId).param("common", common).param("rare", rare).update();
    }

    /** Every plant blooms now and its flower is discovered. */
    void bloomAll(long userId, LocalDate today, Instant now) {
        jdbc.sql(TesterSql.BLOOM_ALL)
                .param("userId", userId)
                .param("waterings", GardenRules.WATERINGS_TO_BLOOM)
                .param("today", Date.valueOf(today))
                .update();
        jdbc.sql(TesterSql.DISCOVER_BLOOMING).param("userId", userId).param("now", Timestamp.from(now)).update();
    }

    private void run(List<String> statements, long userId) {
        statements.forEach(sql -> jdbc.sql(sql).param("userId", userId).update());
    }
}
