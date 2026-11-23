package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Brings the pet up to date with the time that passed and applies care actions. */
@Service
class PetService {

    private final PetRepository pets;
    private final Clock clock;

    PetService(PetRepository pets, Clock clock) {
        this.pets = pets;
        this.clock = clock;
    }

    /** The pet's needs now; the first call hatches the pet. */
    @Transactional
    PetNeeds current(long userId) {
        return update(userId, UnaryOperator.identity());
    }

    @Transactional
    PetNeeds care(long userId, PetAction action) {
        return update(userId, action::applyTo);
    }

    private PetNeeds update(long userId, UnaryOperator<PetNeeds> change) {
        Instant now = clock.instant();
        pets.createIfMissing(userId, now);
        StoredPet stored = pets.findForUpdate(userId);
        PetNeeds needs = change.apply(stored.needs().after(Duration.between(stored.updatedAt(), now)));
        pets.save(userId, needs, now);
        return needs;
    }
}
