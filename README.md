# Tamagotchi bot

A Telegram bot with a pet to look after and, later, party games for group chats (first: a Hat / Alias style word game).
The bot and its Mini App speak English and Russian, picked from the user's Telegram language.

## Services

One repository, two deployable services:

- **backend** (Java 25, Spring Boot 4, Maven modules, one process)
  - `backend/common`: Telegram connection (long polling via [telegrambots](https://github.com/rubenlagus/TelegramBots)), command routing, localized replies, Mini App sign-in.
  - `backend/pet`: the pet. Satiety, mood and energy drop every hour and are kept in PostgreSQL as of the last change;
    `GET /api/v1/pet` brings them up to date, `POST /api/v1/pet/actions/{feed|treat|play|sleep}` applies a care action.
    A pet put to bed sleeps for hours, until its energy is full, and cannot be cared for meanwhile. The user names
    the pet when it arrives and can rename it later with `PUT /api/v1/pet/name`. Stroking the pet
    (`POST /api/v1/pet/stroke`) cheers it up a little, at most every half a minute, and pays no coins.
    Coins are counted on the server, with a ledger of every credit and debit: care earns coins only when the need was low
    (with a daily cap), the day's first `GET /api/v1/wallet` pays a bonus that grows with the streak of days in a row,
    a treat costs coins (plain food stays free), and `/api/v1/decor` sells room decor, bought once and placed one per slot.
    Balance numbers live in `PetRules`. Testers listed in `TESTER_IDS` get tools under `/api/v1/tester` to add coins,
    skip hours or a day and start over with their own pet.
  - `backend/app`: runs the bot and the API; `/start` greeting with an "Open the app" button; `GET /api/v1/me`.
- **frontend** (`frontend/`, React, Vite, TypeScript): the Telegram Mini App. `server.mjs` serves the built pages and proxies `/api` to the backend.

The Mini App sends its Telegram launch data as `Authorization: tma <initData>`. The backend checks Telegram's
signature with the bot token and rejects data older than `TELEGRAM_INIT_DATA_MAX_AGE` (24 hours by default).

## Run locally

Create a bot with [@BotFather](https://t.me/BotFather), copy `.env.example` to `.env` and set `TELEGRAM_BOT_TOKEN` and `DATABASE_PASSWORD`. Then:

```sh
docker compose up --build
```

The Mini App is at `http://localhost:3000`. Outside Telegram it only asks you to open it from the bot. Telegram opens
Mini Apps over HTTPS only, so to try it inside Telegram, expose port 3000 through an HTTPS tunnel and set `MINI_APP_URL` to that address.

Development without Docker:

```sh
# needs PostgreSQL on localhost:5432, database and user "tamagotchi"
TELEGRAM_BOT_TOKEN=... DATABASE_PASSWORD=... sh mvnw -pl backend/app -am spring-boot:run
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
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/tamagotchi` | PostgreSQL address; the schema is created by Flyway on start |
| `DATABASE_USERNAME` | `tamagotchi` | Database user |
| `DATABASE_PASSWORD` | empty | Database password |
| `TESTER_IDS` | empty | Comma-separated Telegram user ids who see the tester tools; keep empty in production |
| `PORT` | `8080` | API and health port |

Frontend: `PORT` (3000), `BACKEND_UPSTREAM` (`http://localhost:8080`).

Secrets live only in the environment; `.env` is not in Git.

## Tests

Integration tests only: `sh mvnw verify`. They start PostgreSQL in Docker (Testcontainers), so Docker must be running.

- `BotIT` starts the whole backend against a local HTTP server that speaks the Bot API, sends user messages through `getUpdates` and checks the replies.
- `PetApiIT` hatches, names, feeds, plays with and puts a pet to sleep through the API, checks that needs drop over time and
  that a sleeping pet regains energy, wakes up and cannot be cared for meanwhile, and that stroking cheers it up at most
  every half a minute.
- `TesterApiIT` checks that only configured testers can add coins, skip hours or a day and start over.
- `CoinsApiIT` earns coins (daily bonus and streak, useful care, daily cap) and spends them on treats and decor.
- `MiniAppApiIT` calls `/api/v1/me` over HTTP with launch data signed the way Telegram signs it, plus changed, expired and missing data.

No requests leave the machine. The frontend is type-checked and built in CI.

## Deploy

Railway, Coolify or any Docker host: PostgreSQL and two services built from the repository root:

- backend: `backend/app/Dockerfile`, health check `/actuator/health/readiness`, exactly one instance (Telegram allows one long polling client per bot), private network only; set `DATABASE_URL`, `DATABASE_USERNAME` and `DATABASE_PASSWORD`.
- frontend: `frontend/Dockerfile`, health check `/healthz`, public domain; set `BACKEND_UPSTREAM` to the backend's private address and put the frontend's domain into the backend's `MINI_APP_URL`.
