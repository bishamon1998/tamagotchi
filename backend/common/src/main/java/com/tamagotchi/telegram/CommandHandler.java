package com.tamagotchi.telegram;

import org.telegram.telegrambots.meta.api.objects.message.Message;

/** Handles one slash command, for example {@code /start}. Each module registers its commands as beans. */
public interface CommandHandler {

    /** Command name without the slash. */
    String command();

    void handle(Message message);
}
