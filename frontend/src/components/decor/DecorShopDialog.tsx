import "./DecorShopDialog.css";
import type { Messages } from "../../i18n/messages";
import type { DecorItem, DecorItemId } from "../../decor/decor";
import { CoinBalance } from "../coins/CoinBalance";
import { ModalDialog } from "../dialog/ModalDialog";
import { DecorShopItem } from "./DecorShopItem";

type Props = {
  open: boolean;
  text: Messages;
  coins: number;
  items: DecorItem[];
  pending: DecorItemId | null;
  failed: boolean;
  onBuy: (item: DecorItemId) => void;
  onPlace: (item: DecorItemId) => void;
  onRemove: (item: DecorItemId) => void;
  onClose: () => void;
};

/** The decor catalogue in a modal dialog, with the balance in its header. */
export function DecorShopDialog({ open, text, coins, items, pending, failed, onBuy, onPlace, onRemove, onClose }: Props) {
  return (
    <ModalDialog
      open={open}
      title={text.shop.title}
      closeLabel={text.shop.close}
      extra={<CoinBalance coins={coins} label={text.coins(coins)} />}
      onClose={onClose}
    >
      <ul className="decor-shop__items">
        {items.map((item) => (
          <DecorShopItem
            key={item.id}
            item={item}
            text={text}
            affordable={coins >= item.price}
            busy={pending === item.id}
            disabled={pending !== null}
            onBuy={() => onBuy(item.id)}
            onPlace={() => onPlace(item.id)}
            onRemove={() => onRemove(item.id)}
          />
        ))}
      </ul>
      {failed && <p className="decor-shop__error" role="alert">{text.careFailed}</p>}
    </ModalDialog>
  );
}
