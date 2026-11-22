package com.tamagotchi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tamagotchi.miniapp.TelegramUser;
import com.tamagotchi.telegram.UserLanguage;

/** Tells the Mini App who opened it and which interface language to use. */
@RestController
class MeController {

    record Me(long id, String firstName, String language) {
    }

    @GetMapping("/api/v1/me")
    Me me(TelegramUser user) {
        return new Me(user.id(), user.firstName(), UserLanguage.of(user.languageCode()).getLanguage());
    }
}
