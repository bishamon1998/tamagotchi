/** Room decor items the backend sells, in shop order. */
export type DecorItemId = "striped-rug" | "dog-bed" | "ceiling-lamp" | "corgi-portrait" | "sea-picture";

/** A spot in the room that holds one placed item. */
export type DecorSlot = "rug" | "bed" | "lamp" | "wall";

/** One catalogue item as the user sees it. */
export type DecorItem = { id: DecorItemId; slot: DecorSlot; price: number; owned: boolean; placed: boolean };

/** The catalogue with the user's balance; every shop call answers with it. */
export type DecorShop = { coins: number; items: DecorItem[] };

/** Ids of the items standing in the room. */
export function placedDecor(items: DecorItem[]): DecorItemId[] {
  return items.filter((item) => item.placed).map((item) => item.id);
}
