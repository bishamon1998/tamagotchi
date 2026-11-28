/** Garden data as the backend sends it (GET /api/v1/garden and every garden action). */
export type SeedKind = "common" | "rare";
export type Stage = "seed" | "sprout" | "bud" | "bloom";
export type Rarity = "common" | "uncommon" | "rare";

/** Every flower that can grow, in the order of the collection book. Mirrors the backend's Flower enum. */
export const FLOWERS = [
  { id: "daisy", rarity: "common" },
  { id: "tulip", rarity: "common" },
  { id: "poppy", rarity: "common" },
  { id: "cornflower", rarity: "common" },
  { id: "sunflower", rarity: "common" },
  { id: "bluebell", rarity: "common" },
  { id: "rose", rarity: "uncommon" },
  { id: "iris", rarity: "uncommon" },
  { id: "lily", rarity: "uncommon" },
  { id: "peony", rarity: "uncommon" },
  { id: "lotus", rarity: "rare" },
  { id: "edelweiss", rarity: "rare" },
] as const satisfies readonly { id: string; rarity: Rarity }[];

export type FlowerId = (typeof FLOWERS)[number]["id"];

/** A plant in a bed; `flower` stays null until it blooms, so nobody knows what grows. */
export type Growing = { stage: Stage; wilting: boolean; wateredToday: boolean; flower: FlowerId | null };

export type Bed = { bed: number; plant: Growing | null };

export type Garden = {
  coins: number;
  seeds: Record<SeedKind, number>;
  /** True in the answer to the day's first visit, which brought the free seed. */
  freeSeed: boolean;
  prices: Record<SeedKind, number>;
  /** What the market pays for a flower of each rarity. */
  flowerPrices: Record<Rarity, number>;
  beds: Bed[];
  /** Picked flowers waiting to be sold or put in the vase, and how many of each. */
  basket: Partial<Record<FlowerId, number>>;
  /** The flower in the vase at home, if any. */
  vase: FlowerId | null;
  discovered: FlowerId[];
};

export function rarityOf(flower: FlowerId): Rarity {
  return FLOWERS.find(({ id }) => id === flower)!.rarity;
}
