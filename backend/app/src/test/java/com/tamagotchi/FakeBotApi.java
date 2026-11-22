package com.tamagotchi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * A local HTTP server that speaks the part of the Telegram Bot API the bot uses.
 * Tests queue user messages for getUpdates and read what the bot sent back.
 */
final class FakeBotApi {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final long POLL_WAIT_MILLIS = 500;

    private final HttpServer server;
    private final BlockingQueue<JsonNode> pendingUpdates = new LinkedBlockingQueue<>();
    private final BlockingQueue<JsonNode> sentMessages = new LinkedBlockingQueue<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    FakeBotApi() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/", this::handle);
        server.start();
    }

    String url() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    /** Queues a private text message from a user, as Telegram would deliver it. */
    void userSends(long userId, String firstName, String languageCode, String text) {
        int id = nextId.getAndIncrement();
        ObjectNode message = JSON.createObjectNode()
                .put("message_id", id)
                .put("date", Instant.now().getEpochSecond())
                .put("text", text);
        message.putObject("chat").put("id", userId).put("type", "private");
        message.putObject("from").put("id", userId).put("is_bot", false)
                .put("first_name", firstName).put("language_code", languageCode);
        pendingUpdates.add(JSON.createObjectNode().put("update_id", id).set("message", message));
    }

    /** Waits for the next sendMessage call and returns its request body. */
    JsonNode nextSentMessage() throws InterruptedException {
        JsonNode sent = sentMessages.poll(10, TimeUnit.SECONDS);
        if (sent == null) {
            throw new AssertionError("The bot sent no message");
        }
        return sent;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Bot API method names are case-insensitive; the client sends them in lower case.
        String method = path.substring(path.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        JsonNode body = JSON.readTree(exchange.getRequestBody());
        ObjectNode response = JSON.createObjectNode().put("ok", true);
        switch (method) {
            case "getupdates" -> response.set("result", takeUpdates());
            case "sendmessage" -> {
                sentMessages.add(body);
                response.set("result", sentMessageResult(body));
            }
            default -> response.put("result", true);
        }
        byte[] bytes = JSON.writeValueAsBytes(response);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    /** Holds the poll briefly like real long polling, so the bot does not spin. */
    private ArrayNode takeUpdates() {
        List<JsonNode> updates = new ArrayList<>();
        try {
            JsonNode first = pendingUpdates.poll(POLL_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            if (first != null) {
                updates.add(first);
                pendingUpdates.drainTo(updates);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return JSON.createArrayNode().addAll(updates);
    }

    private ObjectNode sentMessageResult(JsonNode request) {
        ObjectNode message = JSON.createObjectNode()
                .put("message_id", nextId.getAndIncrement())
                .put("date", Instant.now().getEpochSecond())
                .put("text", request.path("text").asText());
        message.putObject("chat").put("id", request.path("chat_id").asLong()).put("type", "private");
        return message;
    }
}
