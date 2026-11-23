package com.tamagotchi.pet;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tamagotchi.miniapp.TelegramUser;

/** The signed-in user's pet for the Mini App. Needs are whole percentages. */
@RestController
class PetController {

    record Pet(int satiety, int mood, int energy) {

        static Pet of(PetNeeds needs) {
            return new Pet(percent(needs.satiety()), percent(needs.mood()), percent(needs.energy()));
        }
    }

    /** The pet after a care action, the balance after it and the coins the action earned (0 if none). */
    record Care(int satiety, int mood, int energy, int coins, int earned) {

        static Care of(CareOutcome outcome) {
            PetNeeds needs = outcome.needs();
            return new Care(percent(needs.satiety()), percent(needs.mood()), percent(needs.energy()),
                    outcome.coins(), outcome.earned());
        }
    }

    private final PetService pets;

    PetController(PetService pets) {
        this.pets = pets;
    }

    @GetMapping("/api/v1/pet")
    Pet pet(TelegramUser user) {
        return Pet.of(pets.current(user.id()));
    }

    /** {@code action} is feed, treat, play or sleep. A treat the user cannot afford is refused with 409. */
    @PostMapping("/api/v1/pet/actions/{action}")
    Care care(TelegramUser user, @PathVariable String action) {
        return Care.of(pets.care(user.id(), parse(action)));
    }

    /** Rounded up, so a need shows 0 only when it is really empty. */
    private static int percent(double value) {
        return (int) Math.ceil(value);
    }

    private static PetAction parse(String action) {
        return Arrays.stream(PetAction.values())
                .filter(candidate -> candidate.id().equals(action))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown action: " + action));
    }
}
