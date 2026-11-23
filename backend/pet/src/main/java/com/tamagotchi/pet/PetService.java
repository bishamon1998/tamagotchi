package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Brings the pet up to date with the time that passed and applies care actions, paying for or rewarding them. */
@Service
class PetService {

    private final PetRepository pets;
    private final WalletService wallets;
    private final Clock clock;

    PetService(PetRepository pets, WalletService wallets, Clock clock) {
        this.pets = pets;
        this.wallets = wallets;
        this.clock = clock;
    }

    /** The pet's needs now; the first call hatches the pet. */
    @Transactional
    PetNeeds current(long userId) {
        Instant now = clock.instant();
        PetNeeds needs = load(userId, now);
        pets.save(userId, needs, now);
        return needs;
    }

    /** A paid action fails as a whole, leaving the pet as it was, when the user cannot afford it. */
    @Transactional
    CareOutcome care(long userId, PetAction action) {
        Instant now = clock.instant();
        PetNeeds before = load(userId, now);
        if (action.price() > 0) {
            wallets.spend(userId, action.price(), CoinReason.TREAT, action.id(), now);
        }
        PetNeeds after = action.applyTo(before);
        pets.save(userId, after, now);
        int earned = action.earnsCoins(before) ? wallets.rewardCare(userId, action, now) : 0;
        return new CareOutcome(after, wallets.coins(userId), earned);
    }

    /** Locks the pet row so two quick taps cannot both act on the same old state. */
    private PetNeeds load(long userId, Instant now) {
        pets.createIfMissing(userId, now);
        StoredPet stored = pets.findForUpdate(userId);
        return stored.needs().after(Duration.between(stored.updatedAt(), now));
    }
}
