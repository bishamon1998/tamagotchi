import "./MarketCard.css";
import type { Messages } from "../../i18n/messages";
import { FLOWERS, rarityOf, type FlowerId } from "../../garden/garden";
import { flowerImage } from "../../garden/gardenArt";
import { useGarden } from "../../garden/useGarden";
import { CoinBalance } from "../coins/CoinBalance";

type Props = { initData: string; text: Messages };

/** The market: sell picked flowers for coins or take one home for the vase, and buy seeds. */
export function MarketCard({ initData, text }: Props) {
  const { garden, pending, failed, buy, sell, putInVase } = useGarden(initData);
  const words = text.market;

  if (!garden) {
    return (
      <section className="market-card">
        <p className="market-card__note" role={failed ? "alert" : "status"}>{failed ? words.failed : text.loading}</p>
      </section>
    );
  }

  const picked = FLOWERS.map(({ id }) => id).filter((id) => (garden.basket[id] ?? 0) > 0);
  const vase = garden.vase ? text.garden.flowers[garden.vase] : null;

  return (
    <section className="market-card" aria-labelledby="market-title">
      <div className="market-card__heading">
        <h2 className="market-card__title" id="market-title">{words.title}</h2>
        <CoinBalance coins={garden.coins} label={text.coins(garden.coins)} />
      </div>

      <h3 className="market-card__section">{words.basket}</h3>
      {picked.length === 0 && <p className="market-card__note">{words.emptyBasket}</p>}
      <ul className="market-card__flowers">
        {picked.map((flower) => (
          <BasketRow
            key={flower}
            flower={flower}
            count={garden.basket[flower] ?? 0}
            price={garden.flowerPrices[rarityOf(flower)]}
            text={text}
            disabled={pending}
            onSell={() => sell(flower)}
            onVase={() => putInVase(flower)}
          />
        ))}
      </ul>
      <p className="market-card__note">{vase ? words.inVase(vase) : words.emptyVase}</p>

      <h3 className="market-card__section">{words.seeds}</h3>
      <p className="market-card__note">{text.garden.seeds(garden.seeds.common, garden.seeds.rare)}</p>
      <div className="market-card__shop">
        <button type="button" disabled={pending || garden.coins < garden.prices.common} onClick={() => buy("common")}>
          {text.garden.buy.common(garden.prices.common)}
        </button>
        <button type="button" disabled={pending || garden.coins < garden.prices.rare} onClick={() => buy("rare")}>
          {text.garden.buy.rare(garden.prices.rare)}
        </button>
      </div>
      {failed && <p className="market-card__error" role="alert">{words.failed}</p>}
    </section>
  );
}

type RowProps = {
  flower: FlowerId;
  count: number;
  price: number;
  text: Messages;
  disabled: boolean;
  onSell: () => void;
  onVase: () => void;
};

/** One kind of picked flower in the basket: how many, and what to do with one of them. */
function BasketRow({ flower, count, price, text, disabled, onSell, onVase }: RowProps) {
  return (
    <li className="market-card__flower">
      <img className="market-card__image" src={flowerImage(flower)} alt="" />
      <span className="market-card__name">{text.market.count(text.garden.flowers[flower], count)}</span>
      <div className="market-card__actions">
        <button type="button" disabled={disabled} onClick={onSell}>{text.market.sell(price)}</button>
        <button type="button" disabled={disabled} onClick={onVase}>{text.market.toVase}</button>
      </div>
    </li>
  );
}
