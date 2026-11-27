package com.tamagotchi.pet;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Stores one wallet per Telegram user and the ledger of every coin that came in or went out. */
@Repository
class WalletRepository {

    private final JdbcClient jdbc;

    WalletRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Opens an empty wallet unless the user already has one. */
    void createIfMissing(long userId) {
        jdbc.sql(WalletSql.CREATE_IF_MISSING).param("userId", userId).update();
    }

    /** The user's wallet, locked until the transaction ends. Call {@link #createIfMissing} first. */
    Wallet findForUpdate(long userId) {
        return jdbc.sql(WalletSql.FIND_FOR_UPDATE)
                .param("userId", userId)
                .query((row, rowNumber) -> {
                    Date lastBonusDay = row.getDate("last_bonus_day");
                    return new Wallet(row.getInt("coins"), row.getInt("streak"),
                            lastBonusDay == null ? null : lastBonusDay.toLocalDate());
                })
                .single();
    }

    void save(long userId, Wallet wallet) {
        jdbc.sql(WalletSql.SAVE)
                .param("userId", userId)
                .param("coins", wallet.coins())
                .param("streak", wallet.streak())
                .param("lastBonusDay", wallet.lastBonusDay() == null ? null : Date.valueOf(wallet.lastBonusDay()))
                .update();
    }

    /** Logs a credit (positive amount) or a debit (negative amount). */
    void record(long userId, int amount, CoinReason reason, String detail, Instant at) {
        jdbc.sql(WalletSql.RECORD)
                .param("userId", userId)
                .param("amount", amount)
                .param("reason", reason.name())
                .param("detail", detail)
                .param("createdAt", Timestamp.from(at))
                .update();
    }

    int earnedSince(long userId, CoinReason reason, Instant since) {
        return jdbc.sql(WalletSql.EARNED_SINCE)
                .param("userId", userId)
                .param("reason", reason.name())
                .param("since", Timestamp.from(since))
                .query(Integer.class)
                .single();
    }
}
