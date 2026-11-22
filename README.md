# Tamagotchi bot

A Telegram bot with a pet to look after and, later, party games for group chats (first: a Hat / Alias style word game).
The bot and its Mini App speak English and Russian, picked from the user's Telegram language.

## Services

One repository, two deployable services:

- **backend** (Java 25, Spring Boot 4, Maven modules, one process)
  - `backend/common`: Telegram connection (long polling via [telegrambots](https://github.com/rubenlagus/TelegramBots)), command routing, localized replies, Mini App sign-in.
  - `backend/app`: runs the bot and the API; `/start` greeting with an "Open the app" button; `GET /api/v1/me`.
- **frontend** (`frontend/`, React, Vite, TypeScript): the Telegram Mini App. `server.mjs` serves the built pages and proxies `/api` to the backend.

The Mini App sends its Telegram launch data as `Authorization: tma <initData>`. The backend checks Telegram's
signature with the bot token and rejects data older than `TELEGRAM_INIT_DATA_MAX_AGE` (24 hours by default).

## Run locally

Create a bot with [@BotFather](https://t.me/BotFather), copy `.env.example` to `.env` and set `TELEGRAM_BOT_TOKEN`. Then:

```sh
docker compose up --build
```

The Mini App is at `http://localhost:3000`. Outside Telegram it only asks you to open it from the bot. Telegram opens
Mini Apps over HTTPS only, so to try it inside Telegram, expose port 3000 through an HTTPS tunnel and set `MINI_APP_URL` to that address.

Development without Docker:

```sh
TELEGRAM_BOT_TOKEN=... sh mvnw -pl backend/app -am spring-boot:run
cd frontend && pnpm install && pnpm dev
```

## Configuration

Backend:

| Variable | Default | Meaning |
| --- | --- | --- |
| `TELEGRAM_BOT_TOKEN` | none, required | Bot token from BotFather |
| `MINI_APP_URL` | empty | Public HTTPS address of the frontend; empty means no "Open the app" button |
| `TELEGRAM_API_URL` | `https://api.telegram.org` | Bot API address |
| `TELEGRAM_INIT_DATA_MAX_AGE` | `24h` | How long a Mini App sign-in stays valid |
| `PORT` | `8080` | API and health port |

Frontend: `PORT` (3000), `BACKEND_UPSTREAM` (`http://localhost:8080`).

Secrets live only in the environment; `.env` is not in Git.

## Tests

Integration tests only: `sh mvnw verify`.

- `BotIT` starts the whole backend against a local HTTP server that speaks the Bot API, sends user messages through `getUpdates` and checks the replies.
- `MiniAppApiIT` calls `/api/v1/me` over HTTP with launch data signed the way Telegram signs it, plus changed, expired and missing data.

No requests leave the machine. The frontend is type-checked and built in CI.

## Deploy

Railway (or any Docker host), two services built from the repository root:

- backend: `backend/app/Dockerfile`, health check `/actuator/health/readiness`, exactly one instance (Telegram allows one long polling client per bot), private network only.
- frontend: `frontend/Dockerfile`, health check `/healthz`, public domain; set `BACKEND_UPSTREAM` to the backend's private address and put the frontend's domain into the backend's `MINI_APP_URL`.
