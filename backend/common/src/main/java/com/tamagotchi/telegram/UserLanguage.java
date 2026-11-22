package com.tamagotchi.telegram;

import java.util.Locale;

/** Picks the interface language from a Telegram language code: Russian for "ru", English for everything else. */
public final class UserLanguage {

    public static final Locale RUSSIAN = Locale.of("ru");

    private UserLanguage() {
    }

    public static Locale of(String telegramLanguageCode) {
        return telegramLanguageCode != null && telegramLanguageCode.startsWith("ru") ? RUSSIAN : Locale.ENGLISH;
    }
}
