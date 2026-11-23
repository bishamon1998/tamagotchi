import type { DecorItemId } from "../decor/decor";

const englishPlural = new Intl.PluralRules("en");
const russianPlural = new Intl.PluralRules("ru");

/** "1 coin", "5 coins". */
function countEn(count: number, one: string, other: string): string {
  return `${count} ${englishPlural.select(count) === "one" ? one : other}`;
}

/** "1 монета", "2 монеты", "5 монет". */
function countRu(count: number, one: string, few: string, many: string): string {
  const form = russianPlural.select(count);
  return `${count} ${form === "one" ? one : form === "few" ? few : many}`;
}

const coinsEn = (count: number) => countEn(count, "coin", "coins");
const coinsRu = (count: number) => countRu(count, "монета", "монеты", "монет");

/** Interface texts. English is the default; Russian for users whose Telegram language is Russian. */
export const messages = {
  en: {
    loading: "Loading…",
    greeting: (name: string) => `Hi, ${name}!`,
    petTouch: "Pet your corgi",
    petName: "Corgi",
    petAge: "Puppy",
    stats: { satiety: "Satiety", mood: "Mood", energy: "Energy" },
    actions: { feed: "Feed", treat: "Treat", play: "Play", sleep: "Sleep" },
    treatFor: (price: number) => `Give a treat for ${coinsEn(price)}`,
    careFailed: "Did not work. Try again.",
    coins: coinsEn,
    dailyBonus: (coins: number, days: number) =>
      days > 1 ? `+${coinsEn(coins)} for visiting, ${countEn(days, "day", "days")} in a row.` : `+${coinsEn(coins)} for visiting today.`,
    shop: {
      open: "Decorate the room",
      title: "Room decor",
      close: "Close",
      buy: "Buy",
      buyFor: (item: string, price: number) => `Buy ${item} for ${coinsEn(price)}`,
      place: "Place",
      remove: "Remove",
    },
    decor: {
      "striped-rug": "Striped rug",
      "dog-bed": "Dog bed",
      "ceiling-lamp": "Ceiling lamp",
      "corgi-portrait": "Corgi portrait",
      "sea-picture": "Sea picture",
    } satisfies Record<DecorItemId, string>,
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
    actions: { feed: "Покормить", treat: "Угостить", play: "Поиграть", sleep: "Уложить" },
    treatFor: (price: number) => `Угостить за ${coinsRu(price)}`,
    careFailed: "Не получилось. Попробуй ещё раз.",
    coins: coinsRu,
    dailyBonus: (coins: number, days: number) =>
      days > 1
        ? `+${coinsRu(coins)} за визит, ${countRu(days, "день", "дня", "дней")} подряд.`
        : `+${coinsRu(coins)} за визит сегодня.`,
    shop: {
      open: "Обставить комнату",
      title: "Декор комнаты",
      close: "Закрыть",
      buy: "Купить",
      buyFor: (item: string, price: number) => `Купить «${item}» за ${coinsRu(price)}`,
      place: "Поставить",
      remove: "Убрать",
    },
    decor: {
      "striped-rug": "Полосатый коврик",
      "dog-bed": "Лежанка",
      "ceiling-lamp": "Люстра",
      "corgi-portrait": "Портрет корги",
      "sea-picture": "Картина с морем",
    } satisfies Record<DecorItemId, string>,
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
