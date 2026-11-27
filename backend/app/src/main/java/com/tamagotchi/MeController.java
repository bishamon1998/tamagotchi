package com.tamagotchi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tamagotchi.miniapp.TelegramUser;
import com.tamagotchi.miniapp.Testers;
import com.tamagotchi.telegram.UserLanguage;

/** Tells the Mini App who opened it, which interface language to use and whether to show the tester tools. */
@RestController
class MeController {

    record Me(long id, String firstName, String language, boolean tester) {
    }

    private final Testers testers;

    MeController(Testers testers) {
        this.testers = testers;
    }

    @GetMapping("/api/v1/me")
    Me me(TelegramUser user) {
        return new Me(user.id(), user.firstName(), UserLanguage.of(user.languageCode()).getLanguage(),
                testers.includes(user));
    }
}
