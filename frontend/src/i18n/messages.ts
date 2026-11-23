/** Interface texts. English is the default; Russian for users whose Telegram language is Russian. */
export const messages = {
  en: {
    loading: "Loading…",
    greeting: (name: string) => `Hi, ${name}!`,
    petTouch: "Pet your corgi",
    petName: "Corgi",
    petAge: "Puppy",
    stats: { satiety: "Satiety", mood: "Mood", energy: "Energy" },
    actions: { feed: "Feed", play: "Play", sleep: "Sleep" },
    careFailed: "Did not work. Try again.",
    gamesTitle: "Party games",
    soon: "Soon",
    hatTitle: "Hat",
    hatDescription: "Explain words to your team before the time runs out.",
    moreGamesTitle: "More games",
    moreGamesDescription: "New games for your group will appear here.",
    openInTelegram: "Open this app from the bot in Telegram.",
    signInFailed: "Could not sign you in. Close the app and open it again from the bot.",
    serverUnavailable: "The server is not responding. Try again in a minute.",
  },
  ru: {
    loading: "Загрузка…",
    greeting: (name: string) => `Привет, ${name}!`,
    petTouch: "Погладить корги",
    petName: "Корги",
    petAge: "Щенок",
    stats: { satiety: "Сытость", mood: "Настроение", energy: "Энергия" },
    actions: { feed: "Покормить", play: "Поиграть", sleep: "Уложить" },
    careFailed: "Не получилось. Попробуй ещё раз.",
    gamesTitle: "Игры для компании",
    soon: "Скоро",
    hatTitle: "Шляпа",
    hatDescription: "Объясняй слова своей команде, пока не кончилось время.",
    moreGamesTitle: "Другие игры",
    moreGamesDescription: "Здесь появятся новые игры для компании.",
    openInTelegram: "Открой приложение из бота в Telegram.",
    signInFailed: "Не получилось войти. Закрой приложение и открой его снова из бота.",
    serverUnavailable: "Сервер не отвечает. Попробуй через минуту.",
  },
};

export type Language = keyof typeof messages;
export type Messages = (typeof messages)["en"];

/** Russian for "ru…" language codes, English for everything else (same rule as the bot). */
export function languageOf(code: string | undefined): Language {
  return code?.startsWith("ru") ? "ru" : "en";
}
