package com.tamagotchi.pet;

import java.util.Locale;

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

        /** Rounded up, so a need shows 0 only when it is really empty. */
        private static int percent(double value) {
            return (int) Math.ceil(value);
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

    /** {@code action} is feed, play or sleep. */
    @PostMapping("/api/v1/pet/actions/{action}")
    Pet care(TelegramUser user, @PathVariable String action) {
        return Pet.of(pets.care(user.id(), parse(action)));
    }

    private static PetAction parse(String action) {
        try {
            return PetAction.valueOf(action.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown action: " + action);
        }
    }
}
