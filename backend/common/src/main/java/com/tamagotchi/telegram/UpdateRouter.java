package com.tamagotchi.telegram;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

/** Sends each incoming command to the module that owns it. */
@Component
class UpdateRouter extends DefaultLongPollingUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(UpdateRouter.class);

    private final Map<String, CommandHandler> handlers;

    UpdateRouter(List<CommandHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(CommandHandler::command, Function.identity()));
    }

    @Override
    public void consume(Update update) {
        // One broken update must not stop the polling thread, so failures are logged here.
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();
                commandOf(message.getText()).map(handlers::get).ifPresent(handler -> handler.handle(message));
            }
        } catch (RuntimeException e) {
            log.error("Failed to handle update {}", update.getUpdateId(), e);
        }
    }

    /** "/start@MyBot payload" -> "start". */
    private static Optional<String> commandOf(String text) {
        if (!text.startsWith("/")) {
            return Optional.empty();
        }
        String first = text.substring(1).split("\\s+", 2)[0];
        int mention = first.indexOf('@');
        return Optional.of(mention == -1 ? first : first.substring(0, mention));
    }
}
