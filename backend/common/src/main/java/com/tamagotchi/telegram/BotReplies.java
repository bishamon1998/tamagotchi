package com.tamagotchi.telegram;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/** Replies to a message in the sender's language (EN or RU). */
@Component
public class BotReplies {

    private final TelegramClient client;
    private final MessageSource messages;

    BotReplies(TelegramClient client, MessageSource messages) {
        this.client = client;
        this.messages = messages;
    }

    /** Text with the given message key in the user's language. */
    public String text(User user, String key, Object... args) {
        return messages.getMessage(key, args, UserLanguage.of(user == null ? null : user.getLanguageCode()));
    }

    /** Sends the text with the given message key back to the chat of {@code message}. */
    public void reply(Message message, String key, Object... args) {
        reply(message, null, key, args);
    }

    /** Same as {@link #reply(Message, String, Object...)} with buttons under the text. */
    public void reply(Message message, ReplyKeyboard buttons, String key, Object... args) {
        SendMessage reply = new SendMessage(message.getChatId().toString(), text(message.getFrom(), key, args));
        reply.setReplyMarkup(buttons);
        try {
            client.execute(reply);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Telegram rejected a reply to chat " + message.getChatId(), e);
        }
    }
}
