package com.tamagotchi.pet;

import java.time.Duration;
import java.time.Instant;

/** A pet row: needs as of {@code updatedAt}, when it wakes up if it was put to bed, and its name if it has one. */
record StoredPet(PetNeeds needs, Instant updatedAt, Instant asleepUntil, String name) {

    /** The pet at {@code now}: asleep it regains energy until it wakes, awake it gets tired again. */
    PetState stateAt(Instant now) {
        Instant wakeUp = asleepUntil == null ? updatedAt : latest(updatedAt, earliest(asleepUntil, now));
        PetNeeds rested = needs.afterSleeping(Duration.between(updatedAt, wakeUp));
        PetNeeds current = rested.after(Duration.between(wakeUp, now));
        boolean stillAsleep = asleepUntil != null && asleepUntil.isAfter(now);
        return new PetState(current, stillAsleep ? asleepUntil : null, name);
    }

    private static Instant earliest(Instant first, Instant second) {
        return first.isBefore(second) ? first : second;
    }

    private static Instant latest(Instant first, Instant second) {
        return first.isAfter(second) ? first : second;
    }
}
