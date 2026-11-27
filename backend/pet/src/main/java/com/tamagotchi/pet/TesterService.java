package com.tamagotchi.pet;

import java.time.Clock;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tamagotchi.miniapp.TelegramUser;
import com.tamagotchi.miniapp.Testers;

/**
 * Tools for testers to check the game without waiting: extra coins, skipping hours or a day, and starting over. They
 * change only the tester's own pet and wallet; everyone else gets 403 Forbidden.
 */
@Service
class TesterService {

    private final Testers testers;
    private final TesterRepository repository;
    private final WalletService wallets;
    private final Clock clock;

    TesterService(Testers testers, TesterRepository repository, WalletService wallets, Clock clock) {
        this.testers = testers;
        this.repository = repository;
        this.wallets = wallets;
        this.clock = clock;
    }

    @Transactional
    void addCoins(TelegramUser user) {
        wallets.grant(requireTester(user), PetRules.TESTER_COINS, CoinReason.TESTER, clock.instant());
    }

    /** The pet ages by a few hours at once: needs drop, and a sleeping pet sleeps that much longer or wakes up. */
    @Transactional
    void skipHours(TelegramUser user) {
        repository.skipPetTime(requireTester(user), PetRules.TESTER_TIME_SKIP);
    }

    /** The next visit counts as the next game day: a new daily bonus and a fresh care cap. */
    @Transactional
    void skipDay(TelegramUser user) {
        repository.skipDay(requireTester(user));
    }

    /** Deletes the pet, coins and decor; the next visit hatches a new pet as for a new user. */
    @Transactional
    void startOver(TelegramUser user) {
        repository.deleteProgress(requireTester(user));
    }

    private long requireTester(TelegramUser user) {
        if (!testers.includes(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Testers only");
        }
        return user.id();
    }
}
