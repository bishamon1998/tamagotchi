package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    /** The pet now; the first call hatches the pet. */
    @Transactional
    PetState current(long userId) {
        Instant now = clock.instant();
        PetState pet = load(userId, now);
        pets.save(userId, pet, now);
        return pet;
    }

    /**
     * A sleeping pet cannot be cared for (409 Conflict). A paid action fails as a whole, leaving the pet as it was,
     * when the user cannot afford it. Putting the pet to bed makes it sleep until its energy is full.
     */
    @Transactional
    CareOutcome care(long userId, PetAction action) {
        Instant now = clock.instant();
        PetState before = load(userId, now);
        if (before.asleep()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The pet is asleep");
        }
        if (action.price() > 0) {
            wallets.spend(userId, action.price(), CoinReason.TREAT, action.id(), now);
        }
        PetNeeds needs = action.applyTo(before.needs());
        PetState after = new PetState(needs, action.putsToBed() ? now.plus(needs.sleepLength()) : null, before.name());
        pets.save(userId, after, now);
        int earned = action.earnsCoins(before.needs()) ? wallets.rewardCare(userId, action, now) : 0;
        return new CareOutcome(after, wallets.coins(userId), earned);
    }

    /**
     * A stroke cheers the pet up by {@link PetRules#STROKE_MOOD}, at most once per {@link PetRules#STROKE_COOLDOWN};
     * strokes in between leave it as it is. It never pays coins. A sleeping pet cannot be stroked (409 Conflict).
     */
    @Transactional
    PetState stroke(long userId) {
        Instant now = clock.instant();
        PetState before = load(userId, now);
        if (before.asleep()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The pet is asleep");
        }
        if (!pets.markStroked(userId, now, now.minus(PetRules.STROKE_COOLDOWN))) {
            return before;
        }
        PetState after = new PetState(before.needs().plus(0, PetRules.STROKE_MOOD, 0), null, before.name());
        pets.save(userId, after, now);
        return after;
    }

    /**
     * Gives the pet a name, or a new one. The name is trimmed and must be 1 to {@link PetRules#LONGEST_NAME} characters
     * without control characters, otherwise the request is refused with 400 Bad Request.
     */
    @Transactional
    PetState rename(long userId, String name) {
        String trimmed = name == null ? "" : name.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length == 0 || length > PetRules.LONGEST_NAME || trimmed.codePoints().anyMatch(Character::isISOControl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The name must be 1 to " + PetRules.LONGEST_NAME + " characters");
        }
        PetState pet = load(userId, clock.instant());
        pets.rename(userId, trimmed);
        return new PetState(pet.needs(), pet.asleepUntil(), trimmed);
    }

    /** Locks the pet row so two quick taps cannot both act on the same old state. */
    private PetState load(long userId, Instant now) {
        pets.createIfMissing(userId, now);
        return pets.findForUpdate(userId).stateAt(now);
    }
}
