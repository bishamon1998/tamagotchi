package com.tamagotchi.pet;

import java.time.Instant;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tamagotchi.miniapp.TelegramUser;

/** The signed-in user's pet for the Mini App. Needs are whole percentages. */
@RestController
class PetController {

    /**
     * {@code asleepUntil} is when a sleeping pet wakes up, or null while it is awake; {@code name} is null until the
     * user names the pet.
     */
    record Pet(String name, int satiety, int mood, int energy, Instant asleepUntil) {

        static Pet of(PetState pet) {
            PetNeeds needs = pet.needs();
            return new Pet(pet.name(), percent(needs.satiety()), percent(needs.mood()), percent(needs.energy()),
                    pet.asleepUntil());
        }
    }

    /** The pet after a care action, the balance after it and the coins the action earned (0 if none). */
    record Care(int satiety, int mood, int energy, Instant asleepUntil, int coins, int earned) {

        static Care of(CareOutcome outcome) {
            Pet pet = Pet.of(outcome.pet());
            return new Care(pet.satiety(), pet.mood(), pet.energy(), pet.asleepUntil(), outcome.coins(), outcome.earned());
        }
    }

    record Naming(String name) {
    }

    private final PetService pets;

    PetController(PetService pets) {
        this.pets = pets;
    }

    @GetMapping("/api/v1/pet")
    Pet pet(TelegramUser user) {
        return Pet.of(pets.current(user.id()));
    }

    /** Names the pet, or renames it. A blank or too long name is refused with 400. */
    @PutMapping("/api/v1/pet/name")
    Pet rename(TelegramUser user, @RequestBody Naming naming) {
        return Pet.of(pets.rename(user.id(), naming.name()));
    }

    /**
     * {@code action} is feed, treat, play or sleep. Care for a sleeping pet and a treat the user cannot afford are
     * refused with 409.
     */
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
