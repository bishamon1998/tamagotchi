package com.tamagotchi.pet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tamagotchi.miniapp.TelegramUser;

/** Tester tools for the signed-in tester's own game. The Mini App reloads the pet afterwards. */
@RestController
@ResponseStatus(HttpStatus.NO_CONTENT)
class TesterController {

    private final TesterService tester;

    TesterController(TesterService tester) {
        this.tester = tester;
    }

    @PostMapping("/api/v1/tester/coins")
    void addCoins(TelegramUser user) {
        tester.addCoins(user);
    }

    @PostMapping("/api/v1/tester/skip-hours")
    void skipHours(TelegramUser user) {
        tester.skipHours(user);
    }

    @PostMapping("/api/v1/tester/skip-day")
    void skipDay(TelegramUser user) {
        tester.skipDay(user);
    }

    @DeleteMapping("/api/v1/tester/progress")
    void startOver(TelegramUser user) {
        tester.startOver(user);
    }
}
