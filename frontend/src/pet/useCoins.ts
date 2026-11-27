import { useState } from "react";

/** Coins the last action earned; the id changes every time so the same amount pops up again. */
export type CoinGain = { amount: number; id: number };

/** The balance shown on the pet card. Care and the shop report the new balance from the backend. */
export function useCoins(initial: number) {
  const [coins, setCoins] = useState(initial);
  const [gain, setGain] = useState<CoinGain | null>(null);

  function update(next: number, earned = 0) {
    setCoins(next);
    if (earned > 0) setGain((last) => ({ amount: earned, id: (last?.id ?? 0) + 1 }));
  }

  return { coins, gain, update };
}
