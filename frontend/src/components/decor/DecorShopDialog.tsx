import "./DecorShopDialog.css";
import { useEffect, useRef, type MouseEvent } from "react";
import type { Messages } from "../../i18n/messages";
import type { DecorItem, DecorItemId } from "../../decor/decor";
import { CoinBalance } from "../coins/CoinBalance";
import { CloseIcon } from "../icons/CloseIcon";
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

/** The decor catalogue in a modal dialog. Closes with Esc, the close button or a tap outside. */
export function DecorShopDialog({ open, text, coins, items, pending, failed, onBuy, onPlace, onRemove, onClose }: Props) {
  const dialog = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const element = dialog.current;
    if (!element) return;
    if (open && !element.open) element.showModal();
    if (!open && element.open) element.close();
  }, [open]);

  // The dialog has no padding of its own, so a click that lands on the dialog itself is on the backdrop.
  const closeOnBackdrop = (event: MouseEvent<HTMLDialogElement>) => {
    if (event.target === event.currentTarget) onClose();
  };

  return (
    <dialog ref={dialog} className="decor-shop" aria-labelledby="decor-shop-title" onClose={onClose} onClick={closeOnBackdrop}>
      <div className="decor-shop__body">
        <header className="decor-shop__header">
          <h2 className="decor-shop__title" id="decor-shop-title">{text.shop.title}</h2>
          <CoinBalance coins={coins} label={text.coins(coins)} />
          <button className="decor-shop__close" type="button" aria-label={text.shop.close} onClick={onClose}>
            <CloseIcon />
          </button>
        </header>
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
      </div>
    </dialog>
  );
}
