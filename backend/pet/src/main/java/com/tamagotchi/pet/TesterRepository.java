package com.tamagotchi.pet;

import java.time.Duration;
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
        run(List.of(TesterSql.SKIP_BONUS_DAY, TesterSql.SKIP_LEDGER_DAY), userId);
    }

    void deleteProgress(long userId) {
        run(List.of(TesterSql.DELETE_PET, TesterSql.DELETE_WALLET, TesterSql.DELETE_LEDGER, TesterSql.DELETE_DECOR),
                userId);
    }

    private void run(List<String> statements, long userId) {
        statements.forEach(sql -> jdbc.sql(sql).param("userId", userId).update());
    }
}
