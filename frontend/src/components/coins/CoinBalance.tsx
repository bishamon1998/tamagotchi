import "./CoinBalance.css";
import type { CoinGain } from "../../pet/useCoins";
import { CoinIcon } from "../icons/CoinIcon";

type Props = { coins: number; label: string; gain?: CoinGain | null };

/** The user's coins with a short "+2" pop when care earns some. `label` is the balance in words for screen readers. */
export function CoinBalance({ coins, label, gain }: Props) {
  return (
    <span className="coin-balance">
      <CoinIcon />
      <span aria-hidden="true">{coins}</span>
      <span className="visually-hidden" aria-live="polite">{label}</span>
      {gain && <span key={gain.id} className="coin-balance__gain" aria-hidden="true">+{gain.amount}</span>}
    </span>
  );
}
