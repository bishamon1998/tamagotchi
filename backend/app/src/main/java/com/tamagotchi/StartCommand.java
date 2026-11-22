package com.tamagotchi;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import com.tamagotchi.telegram.BotReplies;
import com.tamagotchi.telegram.CommandHandler;
import com.tamagotchi.telegram.TelegramProperties;

/** Greets a user who opens the bot and offers the Mini App when its address is configured. */
@Component
class StartCommand implements CommandHandler {

    private final BotReplies replies;
    private final TelegramProperties properties;

    StartCommand(BotReplies replies, TelegramProperties properties) {
        this.replies = replies;
        this.properties = properties;
    }

    @Override
    public String command() {
        return "start";
    }

    @Override
    public void handle(Message message) {
        InlineKeyboardMarkup buttons = properties.miniAppUrl() == null ? null : openAppButton(message);
        replies.reply(message, buttons, "start.greeting", message.getFrom().getFirstName());
    }

    private InlineKeyboardMarkup openAppButton(Message message) {
        InlineKeyboardButton open = InlineKeyboardButton.builder()
                .text(replies.text(message.getFrom(), "start.open-app"))
                .webApp(new WebAppInfo(properties.miniAppUrl().toString()))
                .build();
        return new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(open)));
    }
}
