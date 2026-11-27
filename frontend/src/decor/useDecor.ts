import { useState } from "react";
import { buyDecor, placeDecor, removeDecor } from "../api";
import type { DecorItem, DecorItemId, DecorShop } from "./decor";

type ShopRequest = (initData: string, item: DecorItemId) => Promise<DecorShop>;

/** The decor shop: buying, placing and removing items. One request at a time, so quick taps do not pile up. */
export function useDecor(initData: string, initial: DecorItem[], onCoins: (coins: number) => void) {
  const [items, setItems] = useState(initial);
  const [pending, setPending] = useState<DecorItemId | null>(null);
  const [failed, setFailed] = useState(false);

  async function change(item: DecorItemId, send: ShopRequest) {
    if (pending) return;
    setPending(item);
    setFailed(false);
    try {
      const shop = await send(initData, item);
      setItems(shop.items);
      onCoins(shop.coins);
    } catch (error) {
      console.error("Decor change failed", error);
      setFailed(true);
    } finally {
      setPending(null);
    }
  }

  return {
    items,
    pending,
    failed,
    buy: (item: DecorItemId) => change(item, buyDecor),
    place: (item: DecorItemId) => change(item, placeDecor),
    remove: (item: DecorItemId) => change(item, removeDecor),
  };
}
