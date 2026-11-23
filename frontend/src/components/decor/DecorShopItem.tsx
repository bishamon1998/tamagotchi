import "./DecorShopItem.css";
import type { Messages } from "../../i18n/messages";
import type { DecorItem } from "../../decor/decor";
import { decorArt } from "../../decor/decorArt";
import { CoinIcon } from "../icons/CoinIcon";

type Props = {
  item: DecorItem;
  text: Messages;
  affordable: boolean;
  busy: boolean;
  disabled: boolean;
  onBuy: () => void;
  onPlace: () => void;
  onRemove: () => void;
};

/** One catalogue row: the art, the name and what can be done with it now (buy, place or remove). */
export function DecorShopItem({ item, text, affordable, busy, disabled, onBuy, onPlace, onRemove }: Props) {
  const name = text.decor[item.id];
  return (
    <li className="decor-shop-item">
      <span className="decor-shop-item__art">
        <img src={decorArt[item.id]} alt="" />
      </span>
      <span className="decor-shop-item__name">{name}</span>
      {!item.owned && (
        <button
          className="decor-shop-item__button decor-shop-item__button--buy"
          type="button"
          aria-label={text.shop.buyFor(name, item.price)}
          aria-busy={busy}
          disabled={disabled || !affordable}
          onClick={onBuy}
        >
          <CoinIcon />
          {item.price}
        </button>
      )}
      {item.owned && (
        <button className="decor-shop-item__button" type="button" aria-busy={busy} disabled={disabled} onClick={item.placed ? onRemove : onPlace}>
          {item.placed ? text.shop.remove : text.shop.place}
        </button>
      )}
    </li>
  );
}
