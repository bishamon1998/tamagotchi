package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** The user's coins: the daily bonus, rewards for useful care and payments. Every change is logged in the ledger. */
@Service
class WalletService {

    private final WalletRepository wallets;
    private final Clock clock;

    WalletService(WalletRepository wallets, Clock clock) {
        this.wallets = wallets;
        this.clock = clock;
    }

    /** Pays the daily bonus on the first visit of a game day; later visits that day just read the wallet. */
    @Transactional
    DailyCheckIn checkIn(long userId) {
        Instant now = clock.instant();
        LocalDate today = gameDay(now);
        Wallet wallet = lock(userId);
        if (wallet.bonusClaimedOn(today)) {
            return new DailyCheckIn(wallet, 0);
        }
        int streak = wallet.streakOn(today);
        int bonus = PetRules.DAILY_BONUS
                + PetRules.DAILY_BONUS_STEP * (Math.min(streak, PetRules.LONGEST_REWARDED_STREAK) - 1);
        Wallet paid = new Wallet(wallet.coins() + bonus, streak, today);
        wallets.save(userId, paid);
        wallets.record(userId, bonus, CoinReason.DAILY_BONUS, null, now);
        return new DailyCheckIn(paid, bonus);
    }

    /** Pays for useful care unless the day's care cap is reached. Returns the coins earned. */
    @Transactional
    int rewardCare(long userId, PetAction action, Instant now) {
        Wallet wallet = lock(userId);
        Instant dayStart = gameDay(now).atStartOfDay(PetRules.DAY_ZONE).toInstant();
        int left = PetRules.DAILY_CARE_CAP - wallets.earnedSince(userId, CoinReason.CARE, dayStart);
        int reward = Math.clamp(left, 0, PetRules.CARE_REWARD);
        if (reward > 0) {
            wallets.save(userId, wallet.withCoins(wallet.coins() + reward));
            wallets.record(userId, reward, CoinReason.CARE, action.id(), now);
        }
        return reward;
    }

    /** Takes the price from the wallet, or refuses with 409 Conflict when the coins are not enough. */
    @Transactional
    void spend(long userId, int price, CoinReason reason, String detail, Instant now) {
        Wallet wallet = lock(userId);
        if (wallet.coins() < price) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough coins");
        }
        wallets.save(userId, wallet.withCoins(wallet.coins() - price));
        wallets.record(userId, -price, reason, detail, now);
    }

    @Transactional
    int coins(long userId) {
        return lock(userId).coins();
    }

    /** The user's wallet, locked until the transaction ends; opens an empty one on first use. */
    @Transactional
    Wallet lock(long userId) {
        wallets.createIfMissing(userId);
        return wallets.findForUpdate(userId);
    }

    private static LocalDate gameDay(Instant instant) {
        return LocalDate.ofInstant(instant, PetRules.DAY_ZONE);
    }
}
