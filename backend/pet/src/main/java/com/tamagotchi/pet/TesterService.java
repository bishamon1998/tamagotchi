package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tamagotchi.miniapp.TelegramUser;
import com.tamagotchi.miniapp.Testers;

/**
 * Tools for testers to check the game without waiting: extra coins, skipping hours or a day, seeds, instant blooms,
 * every flower at once, and starting over. They change only the tester's own game; everyone else gets 403 Forbidden.
 */
@Service
class TesterService {

    private final Testers testers;
    private final TesterRepository repository;
    private final WalletService wallets;
    private final GardenRepository garden;
    private final Clock clock;

    TesterService(Testers testers, TesterRepository repository, WalletService wallets, GardenRepository garden,
                  Clock clock) {
        this.testers = testers;
        this.repository = repository;
        this.wallets = wallets;
        this.garden = garden;
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

    /**
     * The next visit counts as the next game day: a new daily bonus, a fresh care cap, a new free seed and plants that
     * need water again (or wilt if they were not watered).
     */
    @Transactional
    void skipDay(TelegramUser user) {
        repository.skipDay(requireTester(user));
    }

    /** A handful of seeds to plant at once. */
    @Transactional
    void addSeeds(TelegramUser user) {
        repository.addSeeds(requireTester(user), GardenRules.TESTER_COMMON_SEEDS, GardenRules.TESTER_RARE_SEEDS);
    }

    /** Everything in the beds blooms now, so picking and the collection can be checked without waiting days. */
    @Transactional
    void bloomAll(TelegramUser user) {
        Instant now = clock.instant();
        repository.bloomAll(requireTester(user), LocalDate.ofInstant(now, PetRules.DAY_ZONE), now);
    }

    /** One of every flower in the basket, all discovered, to check the market, the vase and the book at once. */
    @Transactional
    void everyFlower(TelegramUser user) {
        long userId = requireTester(user);
        Instant now = clock.instant();
        garden.lockSeeds(userId);
        for (Flower flower : Flower.values()) {
            garden.addToBasket(userId, flower);
            garden.discover(userId, flower, now);
        }
    }

    /** Deletes the pet, coins, decor and garden; the next visit hatches a new pet as for a new user. */
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
