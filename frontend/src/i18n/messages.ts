import type { DecorItemId } from "../decor/decor";
import type { FlowerId, Rarity, Stage } from "../garden/garden";

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

/** "1 h 40 min", "2 h", "25 min". */
function hoursAndMinutes(minutes: number, hour: string, minute: string): string {
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  const parts = [hours > 0 ? `${hours} ${hour}` : "", rest > 0 || hours === 0 ? `${rest} ${minute}` : ""];
  return parts.filter(Boolean).join(" ");
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
    asleep: (minutes: number) => `Asleep. Wakes up in ${hoursAndMinutes(minutes, "h", "min")}.`,
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
    adoption: {
      arrived: "A box arrived for you. Something inside is moving!",
      open: "Open the box",
      hello: "Meet your corgi! What will you call it?",
      name: { label: "Pet's name", placeholder: "Name", save: "Done", failed: "Could not save the name. Try again." },
    },
    rename: {
      open: "Rename",
      title: "New name",
      close: "Close",
      name: { label: "Pet's name", placeholder: "Name", save: "Save", failed: "Could not save the name. Try again." },
    },
    places: { label: "Places", home: "Home", garden: "Garden", market: "Market" },
    market: {
      title: "Market",
      basket: "Basket",
      emptyBasket: "The basket is empty. Pick flowers in the garden.",
      count: (flower: string, count: number) => `${flower} × ${count}`,
      sell: (price: number) => `Sell for ${coinsEn(price)}`,
      toVase: "Put in the vase",
      inVase: (flower: string) => `In the vase at home: ${flower}.`,
      emptyVase: "The vase at home is empty.",
      seeds: "Seeds",
      failed: "Did not work. Try again.",
    },
    garden: {
      title: "Garden",
      freeSeed: "+1 seed for visiting today.",
      chooseBed: "Tap a bed.",
      bed: (number: number) => `Bed ${number}`,
      emptyBed: "Empty bed",
      stages: { seed: "A seed", sprout: "A sprout", bud: "A bud", bloom: "In bloom" } satisfies Record<Stage, string>,
      wilting: "Wilting! Water it today or it dies.",
      bloomed: (flower: string) => `${flower} bloomed!`,
      plant: { common: (count: number) => `Plant a common seed (${count})`, rare: (count: number) => `Plant a rare seed (${count})` },
      water: "Water",
      watered: "Watered today",
      pick: "Pick",
      toBasket: "Into the basket",
      seeds: (common: number, rare: number) => `Seeds: ${common} common, ${rare} rare`,
      buy: { common: (price: number) => `Common seed, ${coinsEn(price)}`, rare: (price: number) => `Rare seed, ${coinsEn(price)}` },
      book: {
        open: (found: number, total: number) => `Flower book ${found}/${total}`,
        title: "Flower book",
        close: "Close",
        unknown: "???",
      },
      rarity: { common: "Common", uncommon: "Uncommon", rare: "Rare" } satisfies Record<Rarity, string>,
      flowers: {
        daisy: "Daisy",
        tulip: "Tulip",
        poppy: "Poppy",
        cornflower: "Cornflower",
        sunflower: "Sunflower",
        bluebell: "Bluebell",
        rose: "Rose",
        iris: "Iris",
        lily: "Lily",
        peony: "Peony",
        lotus: "Lotus",
        edelweiss: "Edelweiss",
      } satisfies Record<FlowerId, string>,
      facts: {
        daisy: "Its name comes from \"day's eye\": it opens at dawn and closes at night.",
        tulip: "In 17th-century Holland a rare tulip bulb could cost as much as a house.",
        poppy: "Red poppies became a symbol of remembrance after the First World War.",
        cornflower: "It grew among wheat so often that it was named after grain fields.",
        sunflower: "Young sunflowers turn their heads to follow the sun across the sky.",
        bluebell: "In spring, bluebells carpet whole woods in Britain.",
        rose: "People have grown roses in gardens for about five thousand years.",
        iris: "It is named after Iris, the Greek goddess of the rainbow.",
        lily: "Lily pollen stains so strongly that florists snip it off.",
        peony: "A peony can bloom in the same spot for decades.",
        lotus: "Its leaves shed water and dirt, so they always stay clean.",
        edelweiss: "It grows high in the Alps; woolly hairs shield it from cold and sun.",
      } satisfies Record<FlowerId, string>,
      failed: "Did not work. Try again.",
    },
    tester: {
      title: "Tester tools",
      coins: "+100 coins",
      skipHours: "Skip 3 hours",
      skipDay: "Next day",
      startOver: "Start over",
      failed: "Did not work.",
      gardenTools: { title: "Garden", seeds: "+5 seeds", bloom: "Bloom everything", flowers: "Every flower to the basket" },
      animations: {
        title: "Animations",
        reactions: { feed: "Eats", treat: "Chews a treat", play: "Hops", touch: "Tapped", pet: "Stroked", annoyed: "Grumbles" },
        moods: { content: "Calm", happy: "Happy", hungry: "Hungry", tired: "Tired", bored: "Bored", asleep: "Asleep" },
        realMood: "Real mood",
      },
    },
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
    asleep: (minutes: number) => `Спит. Проснётся через ${hoursAndMinutes(minutes, "ч", "мин")}.`,
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
    adoption: {
      arrived: "Тебе привезли коробку. Внутри кто-то шевелится!",
      open: "Открыть коробку",
      hello: "Это твой корги! Как его назовёшь?",
      name: { label: "Имя питомца", placeholder: "Имя", save: "Готово", failed: "Не получилось сохранить имя. Попробуй ещё раз." },
    },
    rename: {
      open: "Переименовать",
      title: "Новое имя",
      close: "Закрыть",
      name: { label: "Имя питомца", placeholder: "Имя", save: "Сохранить", failed: "Не получилось сохранить имя. Попробуй ещё раз." },
    },
    places: { label: "Места", home: "Дом", garden: "Сад", market: "Рынок" },
    market: {
      title: "Рынок",
      basket: "Корзинка",
      emptyBasket: "Корзинка пуста. Сорви цветы в саду.",
      count: (flower: string, count: number) => `${flower} × ${count}`,
      sell: (price: number) => `Продать за ${coinsRu(price)}`,
      toVase: "Поставить в вазу",
      inVase: (flower: string) => `В вазе дома: ${flower}.`,
      emptyVase: "Ваза дома пустая.",
      seeds: "Семена",
      failed: "Не получилось. Попробуй ещё раз.",
    },
    garden: {
      title: "Сад",
      freeSeed: "+1 семечко за визит сегодня.",
      chooseBed: "Выбери грядку.",
      bed: (number: number) => `Грядка ${number}`,
      emptyBed: "Пустая грядка",
      stages: { seed: "Семечко", sprout: "Росток", bud: "Бутон", bloom: "Цветёт" } satisfies Record<Stage, string>,
      wilting: "Вянет! Полей сегодня, иначе погибнет.",
      bloomed: (flower: string) => `Распустился цветок: ${flower}!`,
      plant: { common: (count: number) => `Посадить обычное (${count})`, rare: (count: number) => `Посадить редкое (${count})` },
      water: "Полить",
      watered: "Полито сегодня",
      pick: "Сорвать",
      toBasket: "В корзинку",
      seeds: (common: number, rare: number) => `Семена: обычные ${common}, редкие ${rare}`,
      buy: { common: (price: number) => `Обычное, ${coinsRu(price)}`, rare: (price: number) => `Редкое, ${coinsRu(price)}` },
      book: {
        open: (found: number, total: number) => `Гербарий ${found}/${total}`,
        title: "Гербарий",
        close: "Закрыть",
        unknown: "???",
      },
      rarity: { common: "Обычный", uncommon: "Необычный", rare: "Редкий" } satisfies Record<Rarity, string>,
      flowers: {
        daisy: "Ромашка",
        tulip: "Тюльпан",
        poppy: "Мак",
        cornflower: "Василёк",
        sunflower: "Подсолнух",
        bluebell: "Колокольчик",
        rose: "Роза",
        iris: "Ирис",
        lily: "Лилия",
        peony: "Пион",
        lotus: "Лотос",
        edelweiss: "Эдельвейс",
      } satisfies Record<FlowerId, string>,
      facts: {
        daisy: "Отвар ромашки пьют веками: он успокаивает и согревает.",
        tulip: "В Голландии XVII века редкая луковица тюльпана стоила как дом.",
        poppy: "После Первой мировой войны красный мак стал символом памяти.",
        cornflower: "Василёк так часто рос в пшенице и ржи, что его считали сорняком полей.",
        sunflower: "Молодой подсолнух поворачивает голову вслед за солнцем.",
        bluebell: "Весной колокольчики целиком застилают леса Британии.",
        rose: "Розы выращивают в садах около пяти тысяч лет.",
        iris: "Назван в честь Ириды, греческой богини радуги.",
        lily: "Пыльца лилии так пачкает, что флористы её срезают.",
        peony: "Пион может цвести на одном месте десятки лет.",
        lotus: "Листья лотоса отталкивают воду и грязь, поэтому всегда чистые.",
        edelweiss: "Растёт высоко в Альпах, пушистые волоски берегут его от холода и солнца.",
      } satisfies Record<FlowerId, string>,
      failed: "Не получилось. Попробуй ещё раз.",
    },
    tester: {
      title: "Для тестирования",
      coins: "+100 монет",
      skipHours: "Промотать 3 часа",
      skipDay: "Следующий день",
      startOver: "Начать заново",
      failed: "Не получилось.",
      gardenTools: { title: "Сад", seeds: "+5 семян", bloom: "Всё расцвело", flowers: "Все цветы в корзинку" },
      animations: {
        title: "Анимации",
        reactions: { feed: "Ест", treat: "Грызёт косточку", play: "Прыгает", touch: "Тык", pet: "Гладят", annoyed: "Ворчит" },
        moods: { content: "Спокойный", happy: "Радуется", hungry: "Голодный", tired: "Устал", bored: "Скучает", asleep: "Спит" },
        realMood: "Как есть",
      },
    },
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
